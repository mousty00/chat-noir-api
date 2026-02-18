package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.aws.S3Service;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.user.UserDTO;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.exception.AuthenticationException;
import com.mousty00.chat_noir_api.exception.UserException;
import com.mousty00.chat_noir_api.generic.GenericService;
import com.mousty00.chat_noir_api.mapper.UserMapper;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.specification.UserSpecifications;
import com.mousty00.chat_noir_api.util.PageDefaults;
import jakarta.transaction.Transactional;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import static com.mousty00.chat_noir_api.exception.ResourceNotFoundException.ResourceType;

@Service
public class UserService extends GenericService<User, UserDTO, UserRepository, UserMapper> {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final MediaService mediaService;
    private final S3Service s3Service;

    public UserService(UserMapper mapper,
                       UserRepository repo,
                       MediaService mediaService,
                       S3Service s3Service
    ) {
        super(repo, mapper);
        this.mediaService = mediaService;
        this.s3Service = s3Service;
    }

    public ApiResponse<PaginatedResponse<UserDTO>> getUsers(Integer page, Integer size, String username) {
        Pageable pageable = PageDefaults.of(page, size);
        Specification<User> spec = UserSpecifications.hasUsername(username);

        Page<UserDTO> pageResult = repo.findAll(spec, pageable).map(mapper::toDTO);

        return buildSuccessPageResponse(pageResult, "Users retrieved successfully");
    }

    public ApiResponse<UserDTO> getUserById(UUID id) {
        try {
            return getItemById(id, ResourceType.USER);
        } catch (Exception e) {
            throw UserException.userNotFoundById(id);
        }
    }

    @Transactional
    public ApiResponse<?> deleteUser(UUID id) {
        try {
            return deleteItemById(id);
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", id, e);
            throw UserException.userDeleteError(e);
        }
    }

    @Transactional
    public ApiResponse<?> saveUser(UserDTO user) {
        try {
            return saveItem(user);
        } catch (Exception e) {
            log.error("error saving user with id: {}", user.getId(), e);
            throw UserException.userSaveError("error saving user with id %s".formatted(user.getId()), e);
        }
    }

    public ApiResponse<String> uploadProfileImage(MultipartFile imageFile, UUID userId) {
        try {
            mediaService.validateImageFile(imageFile);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            User user = repo.findById(userId).orElseThrow(() -> UserException.userNotFoundById(userId));
            String authUsername = Objects.requireNonNull(auth).getName();

            if (!authUsername.equals(user.getUsername())) {
                throw AuthenticationException.accessDenied();
            }

            String extension = FilenameUtils.getExtension(imageFile.getOriginalFilename());
            String sanitizedExtension = mediaService.sanitizeExtension(extension);
            String imageKey = mediaService.generateUserImageKey(authUsername, userId, sanitizedExtension);
            String fileName = s3Service.uploadFileAsync(imageFile, imageKey).get();

            mediaService.cleanupOldMedia(imageKey);
            user.setImageKey(imageKey);

            repo.save(user);

            String url = s3Service.generatePresignedUrl(imageKey);

            return ApiResponse.success(
                    HttpStatus.OK.value(),
                    "Profile image uploaded successfully: %s".formatted(fileName),
                    url
            );

        } catch (IllegalArgumentException | NoSuchElementException e) {
            log.warn("Profile image upload validation failed: {}", e.getMessage());
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Profile image upload failed for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.internalError("Failed to upload profile image: " + e.getMessage());
        }
    }
}