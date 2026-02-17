package com.mousty00.chat_noir_api.exception;

import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends ApiException {

    private final Map<String, String> errors;

    public ValidationException(String message, Map<String, String> errors) {
        super(message, "VAL_001", HttpStatus.BAD_REQUEST);
        this.errors = errors;
    }

    public ValidationException(String message) {
        super(message, "VAL_002", HttpStatus.BAD_REQUEST);
        this.errors = new HashMap<>();
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}