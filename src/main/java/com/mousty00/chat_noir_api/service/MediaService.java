package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.aws.S3Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final S3Service s3Service;
    private final Logger log = LoggerFactory.getLogger(MediaService.class);

    protected String sanitizeExtension(String extension) {
        return (extension != null && !extension.isEmpty()) ? extension : "jpg";
    }

    protected void cleanupOldMedia(String imageKey) {
        if (imageKey != null) {
            try {
                s3Service.deleteFile(imageKey);
            } catch (Exception e) {
                log.warn("Failed to delete old media: {}", imageKey, e);
            }
        }
    }

    protected void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed (10MB)");
        }
    }

    public String generateCatImageKey(String username, UUID catId, String sanitizedExtension) {
        return String.format("users/%s/cats/%s/media-%d.%s", username, catId, System.currentTimeMillis(), sanitizedExtension);
    }

    public String generateUserImagekey(String username, UUID userId, String sanitizedExtesion) {
        return String.format("users/%s/profile/profile-%d.%s", username, System.currentTimeMillis(), sanitizedExtesion);
    }
}
