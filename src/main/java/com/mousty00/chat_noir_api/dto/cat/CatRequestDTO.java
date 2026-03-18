package com.mousty00.chat_noir_api.dto.cat;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CatRequestDTO(
        @NotNull(message = "name is required") String name,
        @NotNull(message = "color is required") String color,
        UUID categoryId,
        String sourceName
) {
}
