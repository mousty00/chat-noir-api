package com.mousty00.chat_noir_api.dto.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mousty00.chat_noir_api.exception.ApiException;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        String path,
        Map<String, String> details
) {

    public static ErrorResponse of(ApiException ex, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(path)
                .build();
    }

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }

    public ErrorResponse withDetails(Map<String, String> details) {
        return new ErrorResponse(timestamp, status, error, errorCode, message, path, details);
    }
}
