package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.aws.S3Service;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatMedia;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.exception.AuthenticationException;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.repository.CatMediaRepository;
import com.mousty00.chat_noir_api.repository.CatRepository;
import com.mousty00.chat_noir_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.exception.AuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import static com.mousty00.chat_noir_api.exception.CatException.*;

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
    public ApiResponse<String> uploadMedia(UUID catId, MultipartFile imageFile) {
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

            CatMedia catMedia = catMediaRepository.findByCatId(cat.getId())
                    .orElseGet(() -> CatMedia.builder().id(cat.getId()).build());

            String extension = FilenameUtils.getExtension(imageFile.getOriginalFilename());
            String sanitizedExtension = mediaService.sanitizeExtension(extension);
            String imageKey = mediaService.generateCatImageKey(username, catId, sanitizedExtension);
            String fileName = s3Service.uploadFileAsync(imageFile, imageKey).get();

            mediaService.cleanupOldMedia(catMedia.getMediaKey());
            catMedia.setMediaKey(imageKey);
            catMedia.setContentUrl(fileName);
            catMediaRepository.save(catMedia);

            cat.setMedia(catMedia);
            catRepository.save(cat);

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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Media upload failed for cat {}: {}", catId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload media", e);
        }
    }


}
