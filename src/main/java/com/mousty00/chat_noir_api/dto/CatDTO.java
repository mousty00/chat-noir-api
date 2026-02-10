package com.mousty00.chat_noir_api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CatDTO {
    @NotNull(message = "id is required")
    UUID id;
    @NotNull(message = "name is required")
    String name;
    @NotNull(message = "color is required")
    String color;
    CatCategoryDTO category;
    String image;
    String sourceName;
}
