package com.mousty00.chat_noir_api.dto.api;

import lombok.Builder;

@Builder
public record ApiResponse<T>(
        int status,
        boolean error,
        String message,
        boolean success,
        T data
) { }
