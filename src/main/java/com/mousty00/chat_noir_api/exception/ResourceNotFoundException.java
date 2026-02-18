package com.mousty00.chat_noir_api.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.NOT_FOUND);
    }

    public static ResourceNotFoundException of(ResourceType type, UUID id) {
        String message = String.format("%s not found with id: %s",
                type.name().toLowerCase(), id);
        String code = String.format("%s_001", type.name().substring(0, 3).toUpperCase());
        return new ResourceNotFoundException(message, code);
    }

    public static ResourceNotFoundException of(ResourceType type, String field, String value) {
        String message = String.format("%s not found with %s: %s",
                type.name().toLowerCase(), field, value);
        String code = String.format("%s_001", type.name().substring(0, 3).toUpperCase());
        return new ResourceNotFoundException(message, code);
    }

    public enum ResourceType {
        CAT, CATEGORY, USER, RESERVATION
    }
}