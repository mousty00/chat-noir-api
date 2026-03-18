package com.mousty00.chat_noir_api.dto.cat;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CatCategoryDTO(
        UUID id,
        String name,
        String mediaTypeHint
) {
}
