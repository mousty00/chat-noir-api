package com.mousty00.chat_noir_api.dto.aws;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.time.Instant;

@Data
@Builder
public class S3ObjectInputStreamWrapper {
    private InputStream inputStream;
    private String contentType;
    private Long contentLength;
    private String etag;
    private Instant lastModified;
}