use std::{
    collections::HashMap,
    io::{Read, Result},
    net::TcpStream,
    thread::{self, JoinHandle},
};

use xcb::{
    x::{GetKeyboardMapping, Setup, WarpPointer, Window},
    xtest::FakeInput,
    Connection, Xid,
};

// Listens for things like mouse movement or key presses
pub fn run_listener(mut stream: TcpStream) -> JoinHandle<Result<()>> {
    thread::spawn::<_, Result<()>>(move || {
        println!("Got client: {:?}", stream.peer_addr());
        let (conn, screen_num) = xcb::Connection::connect(Some(":2")).unwrap();
        let setup = conn.get_setup();
        let keycode_map = get_keycode_map(&conn, &setup);
        let root = setup.roots().nth(screen_num as usize).unwrap().root();

        let mut prefix = [0];
        loop {
            stream.read_exact(&mut prefix)?;
            match prefix[0] {
                // Move cursor
                0 => {
                    let mut relative = [0];
                    let mut x = [0; 4];
                    let mut y = [0; 4];
                    stream.read_exact(&mut relative)?;
                    stream.read_exact(&mut x)?;
                    stream.read_exact(&mut y)?;
                    let window = if relative[0] == 1 {
                        Window::none()
                    } else {
                        root
                    };
                    warp(&conn, window, i32::from_be_bytes(x), i32::from_be_bytes(y));
                }
                // Mouse
                1 => {
                    let mut params = [0; 2];
                    stream.read_exact(&mut params)?;

                    let detail = params[0];
                    let event = params[1];

                    input(&conn, event, detail);
                }
                // Key
                2 => {
                    let mut params = [0; 2];
                    stream.read_exact(&mut params)?;

                    let detail = keycode_map[params[0] as usize];
                    let event = params[1];

                    input(&conn, event, detail.try_into().unwrap());
                }
                _ => unimplemented!("Unsupported action"),
            }
        }
    })
}

fn warp(conn: &Connection, root: Window, x: i32, y: i32) {
    let x = x.try_into().unwrap();
    let y = y.try_into().unwrap();
    let warp = WarpPointer {
        src_window: Window::none(),
        dst_window: root,
        src_x: 0,
        src_y: 0,
        src_width: 0,
        src_height: 0,
        dst_x: x,
        dst_y: y,
    };

    conn.send_and_check_request(&warp).unwrap();
}

fn input(conn: &Connection, event: u8, detail: u8) {
    let warp = FakeInput {
        r#type: event,
        detail,
        time: 0,
        root: Window::none(),
        root_x: 0,
        root_y: 0,
        deviceid: 0,
    };

    conn.send_and_check_request(&warp).unwrap();
}

fn get_keycode_map(conn: &Connection, setup: &Setup) -> Vec<usize> {
    let request = GetKeyboardMapping {
        first_keycode: setup.min_keycode(),
        count: setup.max_keycode() - setup.min_keycode() + 1,
    };

    let cookie = conn.send_request(&request);
    let response = conn.wait_for_reply(cookie).unwrap();
    let syms = response.keysyms();
    let skip = response.keysyms_per_keycode() as usize;

    let mut sym2code = HashMap::new();
    for (i, sym) in syms.iter().enumerate().filter(|(i, _)| i % skip == 0) {
        sym2code.insert(sym, i / skip + setup.min_keycode() as usize);
    }

    // This vec is a map from protocol key codes to x11 keycodes. The rust x11 library doesn't
    // reexport the constants so I copied them. Hopefully they don't change.
    let mut codes = Vec::new();
    codes.extend('0' as u32..='9' as u32);
    // DPAD
    codes.push(0xff52);
    codes.push(0xff54);
    codes.push(0xff51);
    codes.push(0xff53);

    codes.extend('a' as u32..='z' as u32);
    codes.push(',' as u32);
    codes.push('.' as u32);
    // Left alt
    codes.push(0xffe9);
    // According to Android, it sent a right alt key. We treat that as super instead because you
    // can't capture super from an Android app. This key sym is left super.
    codes.push(0xffeb);
    // Left then right shift
    codes.push(0xffe1);
    codes.push(0xffe2);

    // Tab
    codes.push(0xff09);
    codes.push(' ' as u32);
    // Enter
    codes.push(0xff0d);
    // Backspace
    codes.push(0xff08);
    codes.push('`' as u32);
    codes.push('-' as u32);
    codes.push('=' as u32);
    codes.push('[' as u32);
    codes.push(']' as u32);
    codes.push('\\' as u32);
    codes.push(';' as u32);
    codes.push('\'' as u32);
    codes.push('/' as u32);
    // Page up then down
    codes.push(0xff55);
    codes.push(0xff56);
    // Escape
    codes.push(0xff1b);
    // delete
    codes.push(0xffff);
    // Left then right control
    codes.push(0xffe3);
    codes.push(0xffe4);
    // Caps lock
    codes.push(0xffe5);
    // f1-f12
    codes.extend(0xffbe..=0xffc9);

    codes
        .iter()
        .map(|sym| {
            sym2code
                .get(sym)
                .expect(&format!("Failed to get {} {:?}", sym, sym2code))
                .clone()
        })
        .collect()
}
