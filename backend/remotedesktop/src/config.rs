use figment::{
    providers::{Env, Format, Toml},
    Figment,
};
use serde::Deserialize;
use xdg::BaseDirectories;

use crate::shared::{Entry, Session, SessionInfo};

#[derive(Deserialize)]
pub struct Config {
    pub desktop: Vec<String>,
    pub envs: Vec<Entry>,
    pub bind: String,
}

impl Default for Config {
    fn default() -> Self {
        Config::new()
    }
}

impl Config {
    pub fn new() -> Config {
        let xdg = BaseDirectories::with_prefix("frostop").unwrap();
        let config_path = xdg
            .place_config_file("config.toml")
            .expect("cannot create configuration directory");
        Figment::new()
            .merge(Toml::file(config_path))
            .merge(Env::prefixed("FROSTOP_"))
            .extract()
            .unwrap()
    }

    pub fn merge_with(&self, adding: &SessionInfo) -> SessionInfo {
        SessionInfo {
            id: adding.id.clone(),
            session: Session {
                name: adding.session.name.clone(),
                desktop: [self.desktop.clone(), adding.session.desktop.clone()].concat(),
                envs: self
                    .envs
                    .clone()
                    .into_iter()
                    .chain(adding.session.envs.clone())
                    .collect(),
            },
        }
    }
}
