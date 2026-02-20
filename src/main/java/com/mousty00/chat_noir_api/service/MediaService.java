package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.exception.MediaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String DEFAULT_EXTENSION = "jpg";
    private static final String IMAGE_PATH_PATTERN = "users/%s/cats/%s/media-%d.%s";
    private static final String PROFILE_PATH_PATTERN = "users/%s/profile/profile-%d.%s";
    private final S3Service s3Service;

    public String sanitizeExtension(String extension) {
        return (extension != null && !extension.isEmpty()) ? extension : DEFAULT_EXTENSION;
    }

    public void cleanupOldMedia(String imageKey) {
        if (imageKey != null) {
            try {
                s3Service.deleteFile(imageKey);
                log.debug("Successfully deleted old media: {}", imageKey);
            } catch (Exception e) {
                log.warn("Failed to delete old media: {}", imageKey, e);
                throw MediaException.mediaDeleteError(e);
            }
        }
    }

    public void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed. Received: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(String.format(
                    "File size exceeds maximum allowed (10MB). Current size: %.2f MB",
                    file.getSize() / (1024.0 * 1024.0)
            ));
        }
    }

    public String generateCatImageKey(String username, UUID catId, String sanitizedExtension) {
        return generateKey(IMAGE_PATH_PATTERN, username, catId, sanitizedExtension);
    }

    public String generateUserImageKey(String username, UUID userId, String sanitizedExtension) {
        return generateKey(PROFILE_PATH_PATTERN, username, userId, sanitizedExtension);
    }

    private String generateKey(String pattern, String username, UUID id, String extension) {
        return String.format(pattern, username, id, System.currentTimeMillis(), extension);
    }
}