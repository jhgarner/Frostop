use std::{
    iter,
    process::{Child, Command},
};

use retry::{delay::Fixed, retry};

use crate::{
    error_status_result::ErrorStatusResult,
    shared::{Entry, SessionInfo},
};
use anyhow::Result;

pub fn start_x(session: SessionInfo) -> Result<Vec<Child>> {
    let mut xvfb = Command::new("Xvfb")
        .args([
            &session.display_name(),
            "-screen",
            "0",
            "3000x3000x24",
            "-noreset",
        ])
        .spawn()?;
    let desktop = Command::new(&session.session.desktop[0])
        .args(&session.session.desktop[1..])
        .envs(
            session
                .session
                .envs
                .iter()
                .map(Entry::to_tuple)
                .chain(iter::once(("DISPLAY", session.display_name().as_str()))),
        )
        .spawn()
        .map_err(|err| xvfb.kill().err().unwrap_or(err))?;

    retry(Fixed::from_millis(100).take(5), || {
        xcb::Connection::connect(Some(&session.display_name()))
    })?;
    Ok(vec![xvfb, desktop])
}

pub fn set_resolution(display_name: &str, width: u32, height: u32) -> Result<()> {
    println!("inside set_resolution");
    // Do a kind of double buffering to swap to the new mode
    setup_mode("homeA", display_name, width, height)
        .or_else(|_| setup_mode("homeB", display_name, width, height))?;

    println!("Done set_resolution");
    Ok(())
}

fn setup_mode(name: &str, display_name: &str, width: u32, height: u32) -> Result<()> {
    println!("Starting setup_mode");
    // We don't care if these two deletions fail. They either succeed, fail because the mode
    // doesn't exist, or fail because the mode is active. The last case is a problem, but the later
    // commands will catch that case.
    Command::new("xrandr")
        .args(["--delmode", "screen", name])
        .env("DISPLAY", display_name)
        .output()?;
    Command::new("xrandr")
        .args(["--rmmode", name])
        .env("DISPLAY", display_name)
        .output()?;

    println!("All clean on {}", name);
    // We care if anything else fails
    Command::new("xrandr")
        // Since we don't use a real monitor, none of these other values matter
        .args([
            "--newmode",
            name,
            "0",
            &width.to_string(),
            // &width.to_string(),
            "0",
            "0",
            "0",
            &height.to_string(),
            "0",
            "0",
            "0",
        ])
        .env("DISPLAY", display_name)
        .output()?
        .success()?;
    println!("Created {}", name);
    println!(
        "Running xrand on {} {} {} {} command {}",
        display_name,
        name,
        width,
        height,
        String::from_utf8(
            Command::new("xrandr")
                .env("DISPLAY", display_name)
                .output()?
                .stdout
        )
        .unwrap_or("Bad output".to_string())
    );
    Command::new("xrandr")
        .args(["--addmode", "screen", name])
        .env("DISPLAY", display_name)
        .output()?
        .success()?;
    println!("Mode added {}", name);
    Command::new("xrandr")
        .args(["--output", "screen", "--mode", name])
        .env("DISPLAY", display_name)
        .output()?
        .success()?;
    println!("Done on {}", name);
    Ok(())
}
