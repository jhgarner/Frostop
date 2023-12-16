mod config;
mod connection;
mod control;
mod error_log;
mod error_status_result;
mod ffmpeg;
mod shared;
mod xenv;

use std::{
    collections::HashMap,
    net::{TcpListener, TcpStream},
    process::Child,
};

use anyhow::Result;
use config::Config;
use connection::ClientManager;
use control::run_listener;
use ffmpeg::run_client;
use ffmpeg_next::{device::register_all, log};
use shared::{Params, SessionInfo};
use xenv::start_x;

use crate::error_log::LogAnyError;

pub struct App {
    pub config: Config,
    pub processes: HashMap<SessionInfo, Vec<Child>>,
    pub clients: HashMap<SessionInfo, ClientManager>,
}

impl App {
    fn new() -> App {
        App {
            config: Config::new(),
            processes: HashMap::new(),
            clients: HashMap::new(),
        }
    }

    fn go(&mut self, socket: TcpStream) -> Result<()> {
        let mut client = ClientManager::new(socket);
        match client.read()? {
            Params::Stop { session_info } => {
                if let Some(client) = self.clients.remove(&session_info) {
                    client.stop();
                }
                if let Some(processes) = self.processes.remove(&session_info) {
                    for mut process in processes {
                        let _ = process.kill();
                    }
                }
                client.send(())?;
            }
            Params::Query => {
                let sessions: Vec<SessionInfo> = self.processes.keys().cloned().collect();
                client.send(sessions)?;
            }
            Params::Connect {
                video_params,
                session_info,
            } => {
                if self.processes.get_mut(&session_info).is_none() {
                    let merged_session = self.config.merge_with(&session_info);
                    let processes = start_x(merged_session)?;
                    self.processes.insert(session_info.clone(), processes);
                }
                if let Some(client) = self.clients.remove(&session_info) {
                    client.stop();
                }
                // If rust could clone into closures, that'd be neat
                let display_name1 = session_info.display_name();
                let display_name2 = session_info.display_name();
                let client = self.clients.entry(session_info).or_insert(client);
                println!("Spawning");
                client.spawn(|client| run_client(client, display_name1, video_params))?;
                client.spawn(|client| run_listener(client, display_name2))?;
            }
        };
        Ok(())
    }

    fn clean_client_list(&mut self) {
        self.clients.retain(|_, client| !client.is_stopped())
    }
}

fn main() {
    let mut app = App::new();
    ffmpeg_next::init().unwrap();
    register_all();
    log::set_level(log::Level::Verbose);

    let server = TcpListener::bind(&app.config.bind).unwrap();

    loop {
        app.clean_client_list();
        println!("Waiting on client");
        let (socket, _) = server.accept().unwrap();
        println!("Got client: {:?}", socket.peer_addr());
        app.go(socket).log_on_err();
    }
}
