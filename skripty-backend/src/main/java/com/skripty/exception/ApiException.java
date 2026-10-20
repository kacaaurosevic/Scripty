package com.skripty.exception;

import org.springframework.http.HttpStatus;

/** A controlled, expected error (bad credentials, duplicate email, etc.) with an HTTP status attached. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
