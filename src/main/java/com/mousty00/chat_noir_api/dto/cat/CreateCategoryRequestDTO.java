package com.mousty00.chat_noir_api.dto.cat;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateCategoryRequestDTO(
        @NotBlank(message = "name is required") String name,
        String mediaTypeHint
) {
}
