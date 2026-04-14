package com.mousty00.chat_noir_api.dto.cat;

import com.mousty00.chat_noir_api.entity.SubmissionStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CatSubmissionDTO(
        UUID id,
        UUID userId,
        String name,
        String color,
        CatCategoryDTO category,
        String sourceName,
        String notes,
        String image,
        SubmissionStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        UUID reviewedBy,
        String rejectionReason
) {
}
