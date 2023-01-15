use std::io::{Read, Result, Write};
use std::net::TcpStream;

pub struct StableClient {
    socket: TcpStream,
    pub bitrate: u32,
    pub fps: u8,
    pub width: u32,
    pub height: u32,
}

impl StableClient {
    pub fn new(mut socket: TcpStream) -> Result<StableClient> {
        let mut bitrate = [0; 4];
        socket.read_exact(&mut bitrate)?;
        let bitrate = u32::from_be_bytes(bitrate);
        let mut fps = [0];
        socket.read_exact(&mut fps)?;
        let fps = fps[0];
        let mut width = [0; 4];
        socket.read_exact(&mut width)?;
        let width = u32::from_be_bytes(width);
        let mut height = [0; 4];
        socket.read_exact(&mut height)?;
        let height = u32::from_be_bytes(height);
        Ok(StableClient {
            socket,
            bitrate,
            fps,
            width,
            height,
        })
    }

    pub fn send_all(&mut self, data: &[u8]) -> Result<()> {
        let size: u32 = data.len().try_into().expect("Too big");
        self.socket.write_all(&size.to_be_bytes())?;
        self.socket.write_all(data)
    }
}
