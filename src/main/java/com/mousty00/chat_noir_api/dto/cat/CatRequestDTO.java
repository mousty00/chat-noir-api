package com.mousty00.chat_noir_api.dto.cat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CatRequestDTO {
    @NotNull(message = "name is required")
    String name;
    @NotNull(message = "color is required")
    String color;
    UUID categoryId;
    String sourceName;
}
