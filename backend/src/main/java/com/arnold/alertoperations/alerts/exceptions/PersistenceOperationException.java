package com.arnold.alertoperations.alerts.exceptions;

public class PersistenceOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PersistenceOperationException(Throwable cause) {
        super("A persistence operation failed", cause);
    }
}

