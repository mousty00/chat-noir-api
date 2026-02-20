package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatMediaStreamInfo;
import com.mousty00.chat_noir_api.dto.media.MediaMetadata;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatMedia;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.exception.AuthenticationException;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.exception.MediaException;
import com.mousty00.chat_noir_api.repository.CatMediaRepository;
import com.mousty00.chat_noir_api.repository.CatRepository;
import com.mousty00.chat_noir_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.mousty00.chat_noir_api.exception.CatException.catNotFound;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatMediaService {

    private static final long PRESIGNED_URL_EXPIRY_MINUTES = 15;
    private final S3Service s3Service;
    private final MediaService mediaService;
    private final CatMediaRepository catMediaRepository;
    private final CatRepository catRepository;
    private final UserRepository userRepository;
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public ApiResponse<String> uploadMediaWithCleanup(UUID catId, MultipartFile imageFile) {
        try {
            mediaService.validateImageFile(imageFile);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = Objects.requireNonNull(auth).getName();
            Cat cat = getCatOrThrow(catId);
            User user = getUserOrThrow(username);

            checkUploadPermission(cat, username, user);

            String imageKey = uploadToS3(imageFile, username, catId);
            String oldMediaKey = saveMediaRecord(cat, imageFile, imageKey);

            mediaService.cleanupOldMedia(oldMediaKey);

            String url = s3Service.generatePresignedUrl(imageKey);

            return ApiResponse.<String>builder()
                    .status(HttpStatus.OK.value())
                    .message("Media uploaded successfully")
                    .error(false)
                    .success(true)
                    .data(url)
                    .build();

        } catch (Exception e) {
            return handleUploadException(catId, e);
        }
    }

    public ApiResponse<CatMediaStreamInfo> getCatMediaStreamInfo(UUID id) {
        try {
            MediaMetadata metadata = getMediaMetadata(id);

            CatMediaStreamInfo streamInfo = CatMediaStreamInfo.builder()
                    .streamUrl(baseUrl + "/api/cats/" + id + "/media/stream")
                    .filename(metadata.filename())
                    .contentType(metadata.contentType())
                    .contentLength(metadata.contentLength())
                    .extension(metadata.extension())
                    .viewable(metadata.viewable())
                    .expiresInMinutes(PRESIGNED_URL_EXPIRY_MINUTES)
                    .build();

            return ApiResponse.success(HttpStatus.OK.value(),
                    "Media stream info retrieved successfully", streamInfo);

        } catch (Exception e) {
            log.error("Failed to get media stream info for cat {}: {}", id, e.getMessage());
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Failed to get media stream info: " + e.getMessage());
        }
    }

    public ResponseEntity<StreamingResponseBody> streamCatMedia(UUID catId) {
        try {
            MediaMetadata metadata = getMediaMetadata(catId);

            StreamingResponseBody stream = outputStream -> {
                try (InputStream s3Stream = s3Service.getFileInputStream(metadata.mediaKey())) {
                    IOUtils.copy(s3Stream, outputStream);
                    outputStream.flush();
                }
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + metadata.filename() + "\"")
                    .contentType(MediaType.parseMediaType(metadata.contentType()))
                    .contentLength(metadata.contentLength())
                    .body(stream);

        } catch (Exception e) {
            log.error("Failed to stream media for cat {}: {}", catId, e.getMessage());
            throw e;
        }
    }


    private Cat getCatOrThrow(UUID catId) {
        return catRepository.findById(catId)
                .orElseThrow(() -> catNotFound(catId));
    }

    private User getUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(AuthenticationException::accessDenied);
    }

    private void checkUploadPermission(Cat cat, String username, User user) {
        boolean isUserCreator = Objects.equals(cat.getSourceName(), username);
        if (!isUserCreator && !user.isAdmin()) {
            throw AuthenticationException.accessDenied();
        }
    }

    private String uploadToS3(MultipartFile imageFile, String username, UUID catId) {
        try {
            String extension = FilenameUtils.getExtension(imageFile.getOriginalFilename());
            String sanitizedExtension = mediaService.sanitizeExtension(extension);
            String imageKey = mediaService.generateCatImageKey(username, catId, sanitizedExtension);

            CompletableFuture<String> uploadFuture = s3Service.uploadFileAsync(imageFile, imageKey);
            return uploadFuture.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    private String saveMediaRecord(Cat cat, MultipartFile imageFile, String imageKey) {
        CatMedia catMedia = catMediaRepository.findByCatId(cat.getId())
                .orElse(CatMedia.builder()
                        .cat(cat)
                        .build());

        String oldMediaKey = catMedia.getMediaKey();
        mediaService.cleanupOldMedia(oldMediaKey);

        catMedia.setMediaFormat(imageFile.getContentType());
        catMedia.setMediaKey(imageKey);
        catMedia.setContentUrl(s3Service.generatePresignedUrl(imageKey));

        CatMedia savedMedia = catMediaRepository.save(catMedia);
        cat.setMedia(savedMedia);
        catRepository.save(cat);

        return oldMediaKey;
    }

    private ApiResponse<String> handleUploadException(UUID catId, Exception e) {
        switch (e) {
            case IllegalArgumentException iae -> {
                log.warn("Media upload validation failed for cat {}: {}", catId, iae.getMessage());
                throw MediaException.mediaInvalid(iae);
            }
            case InterruptedException ie -> {
                Thread.currentThread().interrupt();
                log.error("Async upload interrupted for cat {}: {}", catId, ie.getMessage());
                throw MediaException.mediaSaveError(ie);
            }
            case ExecutionException ee -> {
                log.error("Async upload failed for cat {}: {}", catId, ee.getMessage());
                throw MediaException.mediaSaveError(ee);
            }
            default -> {
                log.error("Media upload failed for cat {}: {}", catId, e.getMessage(), e);
                throw MediaException.mediaSaveError(e);
            }
        }
    }

    private MediaMetadata getMediaMetadata(UUID catId) {
        Cat cat = getCatOrThrow(catId);

        if (cat.getMedia() == null || cat.getMedia().getMediaKey() == null) {
            throw CatException.catMediaNotFound(catId);
        }

        String mediaKey = cat.getMedia().getMediaKey();
        String contentType = s3Service.getFileContentType(mediaKey);
        Long contentLength = s3Service.getFileSize(mediaKey);

        return MediaMetadata.from(mediaKey, cat.getName(), contentType, contentLength);
    }
}