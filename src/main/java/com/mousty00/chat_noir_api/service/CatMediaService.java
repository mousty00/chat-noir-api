package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.aws.S3Service;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatMedia;
import com.mousty00.chat_noir_api.repository.CatMediaRepository;
import com.mousty00.chat_noir_api.repository.CatRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
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

@Service
@RequiredArgsConstructor
public class CatMediaService {

    private final Logger log = LoggerFactory.getLogger(CatMediaService.class);
    private final S3Service s3Service;
    private final CatMediaRepository catMediaRepository;
    private final CatRepository catRepository;

    public ApiResponse<String> uploadMedia(UUID catId, MultipartFile imageFile) {

        try {
            if (imageFile.isEmpty()) {
                throw new IllegalArgumentException("Cat media cannot be empty");
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = Objects.requireNonNull(auth).getName();
            Cat cat = catRepository.findById(catId).orElseThrow(() -> new NoSuchElementException("cat not found"));

            // TODO: check if the user is an admin, in that case can proceed
            if (!Objects.equals(cat.getSourceName(), username)) {
                throw new AccessDeniedException("Cat media not editable");
            }

            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String extension = FilenameUtils.getExtension(imageFile.getOriginalFilename());
            String imageKey = String.format("users/%s/profile-%d.%s", auth.getName() ,System.currentTimeMillis(), extension);

            s3Service.uploadFile(imageFile, imageKey);
            CatMedia catMedia = catMediaRepository.findByCatId(cat.getId())
                    .orElseThrow(() -> new NoSuchElementException("Cat media not found"));
            catMediaRepository.saveAndFlush(catMedia);
            String url = s3Service.generatePresignedUrl(imageKey);

            return ApiResponse.<String>builder()
                    .status(HttpStatus.OK.value())
                    .message("Cat media uploaded successfully")
                    .error(false)
                    .success(true)
                    .data(url)
                    .build();

        } catch (Exception e) {
            log.error("Image update failed", e);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Image update failed", e);
        }
    }


}
