package com.mousty00.chat_noir_api.exception;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleApiException(ApiException ex, HttpServletRequest request) {
        log.error("API Exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("errorCode", ex.getErrorCode());

        if (ex instanceof ValidationException && ((ValidationException) ex).getErrors() != null) {
            errorDetails.put("validationErrors", ((ValidationException) ex).getErrors());
        }

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                ex.getStatus().value(),
                ex.getMessage(),
                errorDetails
        );

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("validationErrors", errors);

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid input parameters",
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("validationErrors", errors);

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation: {}", ex.getMessage(), ex);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());

        String message;
        String errorCode;

        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String causeMessage = ex.getCause().getMessage().toLowerCase();

            if (causeMessage.contains("unique")) {
                message = "A record with this value already exists";
                errorCode = "DB_001";
                errorDetails.put("errorCode", errorCode);
            } else if (causeMessage.contains("foreign key")) {
                message = "Referenced record does not exist";
                errorCode = "DB_002";
                errorDetails.put("errorCode", errorCode);
            } else if (causeMessage.contains("not null")) {
                message = "Required field cannot be null";
                errorCode = "DB_003";
                errorDetails.put("errorCode", errorCode);
            } else {
                message = "Database constraint violation";
                errorCode = "DB_004";
                errorDetails.put("errorCode", errorCode);
            }
        } else {
            message = "Database constraint violation";
            errorCode = "DB_004";
            errorDetails.put("errorCode", errorCode);
        }

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                HttpStatus.CONFLICT.value(),
                message,
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("errorCode", "AUTH_001");

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid username or password",
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("errorCode", "AUTH_002");

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                HttpStatus.FORBIDDEN.value(),
                "You don't have permission to access this resource",
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleAllUncaughtException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("exceptionType", ex.getClass().getSimpleName());

        ApiResponse<Map<String, Object>> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                errorDetails
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}