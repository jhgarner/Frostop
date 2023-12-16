use anyhow::{ensure, Result};
use std::process::Output;

pub trait ErrorStatusResult {
    fn success(&self) -> Result<()>;
}

impl ErrorStatusResult for Output {
    fn success(&self) -> Result<()> {
        println!(
            "{}",
            String::from_utf8(self.stdout.clone()).unwrap_or("bad stdout".to_string())
        );
        ensure!(
            self.status.success(),
            String::from_utf8(self.stderr.clone())
                .unwrap_or("Failed with invalid stderr".to_string())
        );
        Ok(())
    }
}
