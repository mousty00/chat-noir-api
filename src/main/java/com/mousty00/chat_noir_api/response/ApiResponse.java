package com.mousty00.chat_noir_api.response;

import lombok.Builder;
import org.springframework.http.HttpStatusCode;

@Builder
public record ApiResponse<T>(
        HttpStatusCode statusCode,
        int status,
        boolean error,
        String message,
        boolean success,
        T data
) { }
