package com.mousty00.chat_noir_api.dto.aws;

import lombok.Builder;

import java.io.InputStream;
import java.time.Instant;

@Builder
public record S3ObjectInputStreamWrapper(
        InputStream inputStream,
        String contentType,
        Long contentLength,
        String etag,
        Instant lastModified
) {
}
