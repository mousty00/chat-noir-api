package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.aws.S3Service;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
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
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.mousty00.chat_noir_api.exception.CatException.catNotFound;

@Service
@RequiredArgsConstructor
public class CatMediaService {

    private final Logger log = LoggerFactory.getLogger(CatMediaService.class);
    private final S3Service s3Service;
    private final MediaService mediaService;
    private final CatMediaRepository catMediaRepository;
    private final CatRepository catRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApiResponse<String> uploadMediaWithCleanup(UUID catId, MultipartFile imageFile) {
        try {
            mediaService.validateImageFile(imageFile);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = Objects.requireNonNull(auth).getName();

            Cat cat = catRepository.findById(catId)
                    .orElseThrow(() -> catNotFound(catId));
            User user = userRepository.findByUsername(username)
                    .orElseThrow(AuthenticationException::accessDenied);

            boolean isUserCreator = Objects.equals(cat.getSourceName(), username);
            if (!isUserCreator && !user.isAdmin()) {
                throw AuthenticationException.accessDenied();
            }

            String extension = FilenameUtils.getExtension(imageFile.getOriginalFilename());
            String sanitizedExtension = mediaService.sanitizeExtension(extension);
            String imageKey = mediaService.generateCatImageKey(username, catId, sanitizedExtension);
            String uploadedKey = s3Service.uploadFileAsync(imageFile, imageKey).get();

            CatMedia catMedia = catMediaRepository.findByCatId(cat.getId())
                    .orElse(CatMedia.builder()
                            .cat(cat)
                            .build());

            String oldMediaKey = catMedia.getMediaKey();

            catMedia.setMediaFormat(imageFile.getContentType());
            catMedia.setMediaKey(imageKey);
            catMedia.setContentUrl(uploadedKey);

            CatMedia savedMedia = catMediaRepository.save(catMedia);

            cat.setMedia(savedMedia);
            catRepository.save(cat);

            if (oldMediaKey != null && !oldMediaKey.equals(imageKey)) {
                try {
                    mediaService.cleanupOldMedia(oldMediaKey);
                } catch (Exception e) {
                    log.warn("Failed to delete old media from S3: {}", oldMediaKey, e);
                }
            }

            String url = s3Service.generatePresignedUrl(imageKey);

            return ApiResponse.<String>builder()
                    .status(HttpStatus.OK.value())
                    .message("Media uploaded successfully")
                    .error(false)
                    .success(true)
                    .data(url)
                    .build();

        } catch (IllegalArgumentException | NoSuchElementException e) {
            log.warn("Media upload validation failed: {}", e.getMessage());
            throw MediaException.mediaInvalid(e);
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Async upload failed for cat {}: {}", catId, e.getMessage());
            throw MediaException.mediaSaveError(e);
        } catch (Exception e) {
            log.error("Media upload failed for cat {}: {}", catId, e.getMessage(), e);
            throw MediaException.mediaSaveError(e);
        }
    }

    @Transactional
    public ApiResponse<Void> deleteMedia(UUID catId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = Objects.requireNonNull(auth).getName();

            Cat cat = catRepository.findById(catId)
                    .orElseThrow(() -> catNotFound(catId));
            User user = userRepository.findByUsername(username)
                    .orElseThrow(AuthenticationException::accessDenied);

            boolean isUserCreator = Objects.equals(cat.getSourceName(), username);
            if (!isUserCreator && !user.isAdmin()) {
                throw AuthenticationException.accessDenied();
            }

            CatMedia media = catMediaRepository.findByCatId(catId)
                    .orElseThrow(() -> CatException.catMediaNotFound(catId));

            cat.setMedia(null);
            catRepository.save(cat);

            try {
                s3Service.deleteFile(media.getMediaKey());
            } catch (Exception e) {
                log.warn("Failed to delete media from S3: {}", media.getMediaKey(), e);
                throw MediaException.mediaDeleteError(e);
            }

            catMediaRepository.delete(media);

            return ApiResponse.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("Media deleted successfully")
                    .error(false)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Media deletion failed for cat {}: {}", catId, e.getMessage(), e);
            throw MediaException.mediaDeleteError(e);
        }
    }
}