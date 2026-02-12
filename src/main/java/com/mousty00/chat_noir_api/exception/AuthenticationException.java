package com.mousty00.chat_noir_api.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApiException {
    
    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNAUTHORIZED);
    }

    public static AuthenticationException badCredentials() {
        return new AuthenticationException("Invalid username or password", "AUTH_001");
    }

    public static AuthenticationException accessDenied() {
        return new AuthenticationException("You don't have permission to access this resource", "AUTH_002");
    }
}