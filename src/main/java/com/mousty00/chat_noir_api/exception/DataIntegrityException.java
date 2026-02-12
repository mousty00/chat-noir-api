package com.mousty00.chat_noir_api.exception;

import org.springframework.http.HttpStatus;

public class DataIntegrityException extends ApiException {
    
    public DataIntegrityException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.CONFLICT);
    }

    public static DataIntegrityException uniqueConstraint(String field) {
        return new DataIntegrityException(
            String.format("A record with this %s already exists", field),
            "DB_001"
        );
    }

    public static DataIntegrityException foreignKeyConstraint(String reference) {
        return new DataIntegrityException(
            String.format("Referenced %s does not exist", reference),
            "DB_002"
        );
    }

    public static DataIntegrityException notNullConstraint(String field) {
        return new DataIntegrityException(
            String.format("Required field '%s' cannot be empty", field),
            "DB_003"
        );
    }
}