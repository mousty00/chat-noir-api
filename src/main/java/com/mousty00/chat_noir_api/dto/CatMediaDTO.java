package com.mousty00.chat_noir_api.dto;

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
public class CatMediaDTO {
    @NotNull(message = "id is required")
    UUID id;
    @NotNull(message = "cat id is required")
    UUID catId;
    @NotNull(message = "format is required")
    String mediaFormat;
    @NotNull(message = "url is required")
    String url;
}
