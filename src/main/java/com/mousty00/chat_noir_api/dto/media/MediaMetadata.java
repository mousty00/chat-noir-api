package com.mousty00.chat_noir_api.dto.media;

import lombok.Builder;

@Builder
public record MediaMetadata(
        String mediaKey,
        String extension,
        String filename,
        String contentType,
        Long contentLength,
        boolean viewable
) {
    public static MediaMetadata from(String mediaKey, String baseName, String contentType, Long contentLength) {
        String extension = "";
        if (mediaKey.contains(".")) {
            extension = mediaKey.substring(mediaKey.lastIndexOf("."));
        }

        String filename = baseName.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                + extension;

        boolean viewable = contentType.startsWith("image/") ||
                contentType.startsWith("video/") ||
                contentType.equals("application/pdf");

        return MediaMetadata.builder()
                .mediaKey(mediaKey)
                .extension(extension.replace(".", ""))
                .filename(filename)
                .contentType(contentType)
                .contentLength(contentLength)
                .viewable(viewable)
                .build();
    }
}