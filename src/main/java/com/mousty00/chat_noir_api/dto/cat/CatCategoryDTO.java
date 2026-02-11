package com.mousty00.chat_noir_api.dto.cat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CatCategoryDTO {
    UUID id;
    String name;
    String mediaTypeHint;
}
