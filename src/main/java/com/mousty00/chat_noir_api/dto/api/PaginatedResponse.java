package com.mousty00.chat_noir_api.dto.api;

import lombok.Builder;

import java.util.List;

@Builder
public record PaginatedResponse<T>(
        List<T> result,
        int currentPage,
        int totalPages,
        long totalItems,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
}
