package com.mousty00.chat_noir_api.dto.user;

import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
public record UserFavoriteDTO(
        UUID id,
        UUID userId,
        UUID catMediaId,
        CatDTO cat
) {
}