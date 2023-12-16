use std::{
    collections::hash_map::DefaultHasher,
    hash::{Hash, Hasher},
};

use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
#[serde(tag = "type")]
pub enum ControlInput {
    Cursor { x: i32, y: i32, relative: bool },
    MouseButton { detail: u8, event: u8 },
    Key { detail: usize, event: u8 },
}

#[derive(Deserialize)]
#[serde(tag = "type")]
pub enum Params {
    Connect {
        video_params: VideoParams,
        session_info: SessionInfo,
    },
    Stop {
        session_info: SessionInfo,
    },
    Query,
}

#[derive(Deserialize)]
pub struct VideoParams {
    pub bitrate: u32,
    pub fps: u8,
    pub width: u32,
    pub height: u32,
}

#[derive(Deserialize, Serialize, PartialEq, Eq, Hash, Clone)]
pub struct SessionInfo {
    pub id: String,
    pub session: Session,
}

impl SessionInfo {
    pub fn display_name(&self) -> String {
        let mut hasher = DefaultHasher::new();
        self.hash(&mut hasher);
        let number = hasher.finish();
        format!(":{}", number);
        ":2".to_string()
    }
}

#[derive(Deserialize, Serialize, Hash, PartialEq, Eq, Clone)]
pub struct Session {
    pub name: String,
    pub desktop: Vec<String>,
    pub envs: Vec<Entry>,
}

#[derive(Deserialize, Serialize, Hash, PartialEq, Eq, Clone)]
pub struct Entry {
    pub key: String,
    pub value: String,
}

impl Entry {
    pub fn new(key: String, value: String) -> Entry {
        Entry { key, value }
    }
}

impl Entry {
    pub fn to_tuple(&self) -> (&str, &str) {
        (&self.key, &self.value)
    }
}
