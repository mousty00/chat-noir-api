package com.mousty00.chat_noir_api.dto.user;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserDTO(
        UUID id,
        String username,
        String email,
        Instant subscriptionStartDate,
        Instant subscriptionEndDate,
        Instant createdAt,
        String image
) {
}
