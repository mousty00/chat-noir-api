package com.mousty00.chat_noir_api.dto.cat;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CatMediaDTO(
        @NotNull(message = "id is required") UUID id,
        @NotNull(message = "cat id is required") UUID catId,
        @NotNull(message = "format is required") String mediaFormat,
        @NotNull(message = "url is required") String url
) {
}
