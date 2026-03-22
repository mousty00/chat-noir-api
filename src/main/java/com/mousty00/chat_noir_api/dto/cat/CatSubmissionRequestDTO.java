package com.mousty00.chat_noir_api.dto.cat;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CatSubmissionRequestDTO(
        @NotNull(message = "name is required") String name,
        String color,
        CatCategoryDTO category,
        String sourceName,
        String notes
) {
}
