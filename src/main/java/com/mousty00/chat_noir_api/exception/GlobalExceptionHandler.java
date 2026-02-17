package com.mousty00.chat_noir_api.exception;

import com.mousty00.chat_noir_api.dto.api.ErrorResponse;
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
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        log.error("API Exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        ErrorResponse response = ErrorResponse.of(ex, request.getRequestURI());

        if (ex instanceof ValidationException && ((ValidationException) ex).getErrors() != null) {
            response.withDetails(((ValidationException) ex).getErrors());
        }

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationException validationEx = new ValidationException("Invalid input parameters", errors);
        ErrorResponse response = ErrorResponse.of(validationEx, request.getRequestURI())
                .withDetails(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        ValidationException validationEx = new ValidationException("Validation failed", errors);
        ErrorResponse response = ErrorResponse.of(validationEx, request.getRequestURI())
                .withDetails(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation: {}", ex.getMessage(), ex);

        DataIntegrityException dataIntegrityEx;

        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            String causeMessage = ex.getCause().getMessage().toLowerCase();

            if (causeMessage.contains("unique")) {
                dataIntegrityEx = DataIntegrityException.uniqueConstraint("field");
            } else if (causeMessage.contains("foreign key")) {
                dataIntegrityEx = DataIntegrityException.foreignKeyConstraint("record");
            } else if (causeMessage.contains("not null")) {
                dataIntegrityEx = DataIntegrityException.notNullConstraint("required");
            } else {
                dataIntegrityEx = new DataIntegrityException(
                        "Database constraint violation", "DB_004");
            }
        } else {
            dataIntegrityEx = new DataIntegrityException(
                    "Database constraint violation", "DB_004");
        }

        ErrorResponse response = ErrorResponse.of(dataIntegrityEx, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {

        AuthenticationException authEx = AuthenticationException.badCredentials();
        ErrorResponse response = ErrorResponse.of(authEx, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        AuthenticationException authEx = AuthenticationException.accessDenied();
        ErrorResponse response = ErrorResponse.of(authEx, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}