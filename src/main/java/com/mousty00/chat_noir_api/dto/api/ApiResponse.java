package com.mousty00.chat_noir_api.dto.api;

import lombok.Builder;

@Builder
public record ApiResponse<T>(
        int status,
        boolean error,
        String message,
        boolean success,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .error(false)
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .error(false)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .error(false)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status(400)
                .error(true)
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .error(true)
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .error(true)
                .success(false)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .status(404)
                .error(true)
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .status(400)
                .error(true)
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .status(401)
                .error(true)
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return ApiResponse.<T>builder()
                .status(403)
                .error(true)
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> internalError(String message) {
        return ApiResponse.<T>builder()
                .status(500)
                .error(true)
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}