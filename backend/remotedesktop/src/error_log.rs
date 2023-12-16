use anyhow::Error;

pub trait LogAnyError {
    fn log_on_err(self);
}

impl<T, E: Into<Error>> LogAnyError for Result<T, E> {
    fn log_on_err(self) {
        if let Err(err) = self {
            let error: Error = err.into();
            eprintln!("Unwrapped Err: {:?}:\n{}", error, error.backtrace());
        }
    }
}
