package com.mousty00.chat_noir_api.exception;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public ApiException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ApiException(String message, String errorCode, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return status; }
}