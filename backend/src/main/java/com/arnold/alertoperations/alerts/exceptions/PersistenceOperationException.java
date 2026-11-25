package com.arnold.alertoperations.alerts.exceptions;

public class PersistenceOperationException extends RuntimeException {

    public PersistenceOperationException(Throwable cause) {
        super("A persistence operation failed", cause);
    }
}

