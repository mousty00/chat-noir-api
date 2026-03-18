package com.mousty00.chat_noir_api.dto.cat;

import lombok.Builder;

@Builder
public record CatMediaStreamInfo(
        String streamUrl,
        String filename,
        String contentType,
        Long contentLength,
        String extension,
        boolean viewable,
        long expiresInMinutes
) {
}
