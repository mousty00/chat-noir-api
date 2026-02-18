package com.mousty00.chat_noir_api.dto.cat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CatMediaStreamInfo {
    private String streamUrl;
    private String filename;
    private String contentType;
    private Long contentLength;
    private String extension;
    private boolean viewable;
    private long expiresInMinutes;
}