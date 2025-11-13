package com.example.blockly_executor_service.exception;

public class BlocklyException extends RuntimeException {
    public BlocklyException() {
    }

    public BlocklyException(String message) {
        super(message);
    }

    public BlocklyException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlocklyException(Throwable cause) {
        super(cause);
    }

    public BlocklyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
