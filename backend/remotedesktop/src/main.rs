mod connection;
mod control;
mod config;

use std::{
    net::TcpListener,
    process::{Command, Output},
};

use config::Config;
use ffmpeg_next::{
    self,
    codec::{Context, Parameters},
    device::register_all,
    encoder::{find_by_name, Video},
    ffi::{avcodec_alloc_context3, AVPixelFormat},
    format::{
        context::{input::dump, Input},
        open_with, Pixel,
    },
    log,
    software::scaling::{self, Flags},
    util::dictionary::Owned,
    Format, Frame, Packet,
};

use crate::{connection::StableClient, control::run_listener};

fn main() {
    let config = Config::new();
    ffmpeg_next::init().unwrap();
    register_all();
    log::set_level(log::Level::Verbose);
    start_x(&config);

    let server = TcpListener::bind(config.bind).unwrap();

    loop {
        println!("Waiting on client");
        let (socket, _) = server.accept().unwrap();
        println!("Got client: {:?}", socket.peer_addr());
        if let Ok(client) = StableClient::new(socket.try_clone().unwrap()) {
            println!("Running listerner with client: {:?}", socket.peer_addr());
            let handle = run_listener(socket);
            run_client(client);
            println!("Waiting on thread to exit");
            println!("Thread exitited with: {:?}", handle.join());
        }
    }
}

// Create Xvfb (the x server) and spawn the user's desktop inside
fn start_x(config: &Config) {
    Command::new("Xvfb")
        .args([":2", "-screen", "0", "3000x3000x24"])
        .spawn()
        .unwrap();
    Command::new(&config.desktop[0])
        .args(&config.desktop[1..])
        .envs(&config.envs)
        .spawn()
        .unwrap();
}

// Send a stream of packets to the client
fn run_client(mut client: StableClient) {
    let mut x11_grab = create_x11_grab(client.fps, client.width, client.height);
    let mut encoder = create_hevc(client.bitrate, client.fps, client.width, client.height);

    for (stream, mut packet) in x11_grab.packets() {
        let mut video = stream.codec().decoder().video().unwrap();
        let mut frame = unsafe { Frame::empty() };
        video.send_packet(&mut packet).unwrap();
        video.receive_frame(&mut frame).unwrap();
        let mut sws = scaling::context::Context::get(
            video.format(),
            video.width(),
            video.height(),
            encoder.format(),
            encoder.width(),
            encoder.height(),
            Flags::BICUBIC,
        )
        .unwrap();
        let mut new_frame = unsafe { Frame::empty().into() };
        sws.run(&frame.into(), &mut new_frame).unwrap();
        encoder.send_frame(&new_frame).unwrap();
        let mut packet = Packet::empty();
        if encoder.receive_packet(&mut packet).is_ok() {
            let data = packet.data().unwrap();
            if let Err(err) = client.send_all(data) {
                println!("Error: {:?}", err);
                break;
            }
        } else {
            println!("DELAYING");
        }
    }
}

// Configure the x11 screen grab based on the client's settings
fn create_x11_grab(fps: u8, width: u32, height: u32) -> Input {
    // Do a kind of double buffering to swap to the new mode
    setup_mode("homeA", width, height)
        .or_else(|_| setup_mode("homeB", width, height))
        .unwrap();

    let mut videos = ffmpeg_next::device::input::video();
    let x11_grab: Format = videos.find(|video| video.name() == "x11grab").unwrap();
    println!("{}", x11_grab.description());

    let mut settings = Owned::new();
    settings.set("video_size", &format!("{}x{}", width, height));
    settings.set("framerate", &fps.to_string());

    let context = open_with(&":2.0+0,0", &x11_grab, settings).unwrap();
    let input = context.input();
    dump(&input, 0, Some(":2.0"));
    input
}

fn setup_mode(name: &str, width: u32, height: u32) -> Result<(), String> {
    // We don't care if these two deletions fail. They either succeed, fail because the mode
    // doesn't exist, or fail because the mode is active. The last case is a problem, but the later
    // commands will catch that case.
    println!(
        "{:?}",
        String::from_utf8(Command::new("xrandr").output().unwrap().stdout)
    );
    Command::new("xrandr")
        .args(["--delmode", "screen", name])
        .env("DISPLAY", ":2")
        .output()
        .unwrap();
    Command::new("xrandr")
        .args(["--rmmode", name])
        .env("DISPLAY", ":2")
        .output()
        .unwrap();

    // We care if anything else fails
    Command::new("xrandr")
        // Since we don't use a real monitor, none of these other values matter
        .args([
            "--newmode",
            name,
            "0",
            &width.to_string(),
            "0",
            "0",
            "0",
            &height.to_string(),
            "0",
            "0",
            "0",
        ])
        .env("DISPLAY", ":2")
        .output()
        .unwrap()
        .success()?;
    Command::new("xrandr")
        .args(["--addmode", "screen", name])
        .env("DISPLAY", ":2")
        .output()
        .unwrap()
        .success()?;
    Command::new("xrandr")
        .args(["--output", "screen", "--mode", name])
        .env("DISPLAY", ":2")
        .output()
        .unwrap()
        .success()
}

fn create_hevc(bitrate: u32, fps: u8, width: u32, height: u32) -> Video {
    // TODO Don't hardcode nvidia hevc. Requires frontend work to negotiate.
    let hevc = find_by_name("hevc_nvenc").unwrap();
    let mut parameters = Parameters::new();
    unsafe {
        let params = parameters.as_mut_ptr();
        (*params).format = AVPixelFormat::AV_PIX_FMT_YUV444P as i32;
        (*params).video_delay = 0;
    }
    let real_context;
    unsafe {
        let raw_context = avcodec_alloc_context3(hevc.as_ptr());
        real_context = Context::wrap(raw_context, None);
    }
    let the_encoder = ffmpeg_next::codec::encoder::encoder::Encoder(real_context);
    let mut video = the_encoder.video().unwrap();
    video.set_parameters(parameters).unwrap();
    video.set_time_base((1, fps.into()));
    video.set_format(Pixel::YUV420P);
    video.set_width(width);
    video.set_height(height);
    video.set_bit_rate(bitrate as usize);
    video.set_threading(ffmpeg_next::threading::Config::count(1));
    // TODO Make these customizable
    let mut settings = Owned::new();
    settings.set("preset", "llhp");
    settings.set("zerolatency", "1");
    settings.set("delay", "0");
    video.open_as_with(hevc, settings).unwrap()
}

trait ErrorStatusResult {
    fn success(&self) -> Result<(), String>;
}

impl ErrorStatusResult for Output {
    fn success(&self) -> Result<(), String> {
        self.status
            .success()
            .then_some(())
            .ok_or(String::from_utf8(self.stderr.clone()).unwrap())
    }
}
