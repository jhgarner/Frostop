use crate::{connection::Client, shared::VideoParams, xenv::set_resolution};

use anyhow::{anyhow, Result};
use ffmpeg_next::{
    self,
    codec::{Context, Parameters},
    encoder::{find_by_name, Video},
    ffi::{avcodec_alloc_context3, AVPixelFormat},
    format::{
        context::{input::dump, Input},
        open_with, Pixel,
    },
    software::scaling::{self, Flags},
    util::dictionary::Owned,
    Format, Frame, Packet,
};

// Configure the x11 screen grab based on the client's settings
fn create_x11_grab(display_name: &str, fps: u8, width: u32, height: u32) -> Result<Input> {
    println!("Setting the resolution");
    set_resolution(display_name, width, height)?;
    println!("Set the resolution");

    let mut videos = ffmpeg_next::device::input::video();
    let x11_grab: Format = videos
        .find(|video| video.name() == "x11grab")
        .ok_or(anyhow!("Could not load x11"))?;
    println!("Creating x11 grab {}", x11_grab.description());

    let mut settings = Owned::new();
    settings.set("video_size", &format!("{}x{}", width, height));
    settings.set("framerate", &fps.to_string());

    let context = open_with(&":2.0+0,0", &x11_grab, settings)?;
    let input = context.input();
    dump(&input, 0, Some(":2.0"));
    Ok(input)
}

fn create_hevc(bitrate: u32, fps: u8, width: u32, height: u32) -> Result<Video> {
    // TODO Don't hardcode nvidia hevc. Requires frontend work to negotiate.
    let hevc = find_by_name("hevc_nvenc").ok_or(anyhow!("Could not find hevc_nvenc"))?;
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
    let mut video = the_encoder.video()?;
    video.set_parameters(parameters)?;
    video.set_time_base((1, fps.into()));
    video.set_format(Pixel::YUV420P);
    video.set_width(width);
    video.set_height(height);
    video.set_bit_rate(bitrate as usize);
    video.set_threading(ffmpeg_next::threading::Config::count(1));
    // TODO Make these customizable
    let mut settings = Owned::new();
    settings.set("preset", "p1");
    settings.set("tune", "ull");
    settings.set("b_ref_mode", "disabled");
    settings.set("g", "1000");
    settings.set("zerolatency", "1");
    settings.set("delay", "0");
    Ok(video.open_as_with(hevc, settings)?)
}

// Send a stream of packets to the client
pub fn run_client(mut client: Client, display_name: String, settings: VideoParams) -> Result<()> {
    println!("Running client");
    let mut x11_grab =
        create_x11_grab(&display_name, settings.fps, settings.width, settings.height)?;
    let mut encoder = create_hevc(
        settings.bitrate,
        settings.fps,
        settings.width,
        settings.height,
    )?;

    println!("Entering client loop");
    for (stream, packet) in x11_grab.packets() {
        let mut video = Context::from_parameters(stream.parameters())?
            .decoder()
            .video()?;
        let mut frame = unsafe { Frame::empty() };
        video.send_packet(&packet)?;
        video.receive_frame(&mut frame)?;
        let mut sws = scaling::context::Context::get(
            video.format(),
            video.width(),
            video.height(),
            encoder.format(),
            encoder.width(),
            encoder.height(),
            Flags::BICUBIC,
        )?;
        let mut new_frame = unsafe { Frame::empty().into() };
        sws.run(&frame.into(), &mut new_frame)?;
        encoder.send_frame(&new_frame)?;
        let mut packet = Packet::empty();
        if encoder.receive_packet(&mut packet).is_ok() {
            let data = packet.data().ok_or(anyhow!("No data in packet"))?;
            client.send_all_blocking(data)?;
        } else {
            println!("Delaying");
        }
    }
    println!("Completing Streaming Thread");
    Ok(())
}
