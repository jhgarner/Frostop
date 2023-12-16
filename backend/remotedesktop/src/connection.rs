use std::io::Write;
use std::net::{Shutdown, TcpStream};
use std::thread::{spawn, JoinHandle};

use anyhow::Result;
use serde::de::DeserializeOwned;
use serde::Serialize;

use crate::error_log::LogAnyError;

pub struct Client {
    socket: TcpStream,
}

pub struct ClientManager {
    stream: TcpStream,
    threads: Vec<JoinHandle<()>>,
}

impl ClientManager {
    pub fn new(stream: TcpStream) -> ClientManager {
        ClientManager {
            stream,
            threads: Vec::new(),
        }
    }

    pub fn spawn<F: FnOnce(Client) -> Result<()> + Send + 'static>(&mut self, f: F) -> Result<()> {
        let new_stream = self.stream.try_clone()?;
        let client = Client::new(new_stream);
        self.threads.push(spawn(move || f(client).log_on_err()));
        Ok(())
    }

    pub fn send<T: Serialize + Send + 'static>(&mut self, t: T) -> Result<()> {
        let data = serde_json::to_vec(&t)?;
        Ok(self.stream.write_all(&data)?)
    }

    pub fn read<T: DeserializeOwned>(&mut self) -> Result<T> {
        let mut reader = serde_json::Deserializer::from_reader(&self.stream);
        Ok(T::deserialize(&mut reader)?)
    }

    pub fn stop(self) {
        println!("Stopping");
        self.stream.shutdown(Shutdown::Both).log_on_err();
        for thread in self.threads {
            thread.join().unwrap();
        }
    }

    pub fn is_stopped(&self) -> bool {
        self.threads.iter().all(|thread| thread.is_finished())
    }
}

impl Client {
    pub fn new(socket: TcpStream) -> Client {
        Client { socket }
    }

    pub fn send_all_blocking(&mut self, data: &[u8]) -> Result<()> {
        let size: u32 = data.len().try_into().expect("Too big");
        self.socket.write_all(&size.to_be_bytes())?;
        Ok(self.socket.write_all(data)?)
    }

    pub fn read<T: DeserializeOwned>(&mut self) -> Result<T> {
        let mut reader = serde_json::Deserializer::from_reader(&self.socket);
        Ok(T::deserialize(&mut reader)?)
    }
}
