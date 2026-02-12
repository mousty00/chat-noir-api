package com.mousty00.chat_noir_api.dto.cat;

import lombok.Builder;

@Builder
public record CatFilterDTO (
        String category,
        String color,
        String name,
        String source
) { }
