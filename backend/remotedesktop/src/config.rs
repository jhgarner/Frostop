use std::collections::HashMap;
use serde::Deserialize;
use figment::{Figment, providers::{Format, Toml, Env}};
use xdg::BaseDirectories;


#[derive(Deserialize)]
pub struct Config {
    pub desktop: Vec<String>,
    pub envs: HashMap<String, String>,
    pub bind: String,
}

impl Config {
    pub fn new() -> Config {
        let xdg = BaseDirectories::with_prefix("frostop").unwrap();
        let config_path = xdg.place_config_file("config.toml").expect("cannot create configuration directory");
        Figment::new()
            .merge(Toml::file(config_path))
            .merge(Env::prefixed("FROSTOP_"))
            .extract()
            .unwrap()
    }
}
