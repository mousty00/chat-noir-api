package com.mousty00.chat_noir_api.dto.user;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserFavoriteDTO(
        UUID id,
        UUID userId,
        UUID catMediaId
) {
}
