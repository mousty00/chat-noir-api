package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.dto.cat.CatSubmissionDTO;
import com.mousty00.chat_noir_api.dto.cat.CatSubmissionRequestDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatCategory;
import com.mousty00.chat_noir_api.entity.CatMedia;
import com.mousty00.chat_noir_api.entity.CatSubmission;
import com.mousty00.chat_noir_api.entity.SubmissionStatus;
import com.mousty00.chat_noir_api.exception.AuthenticationException;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.exception.CatSubmissionException;
import com.mousty00.chat_noir_api.mapper.CatCategoryMapper;
import com.mousty00.chat_noir_api.repository.CatCategoryRepository;
import com.mousty00.chat_noir_api.repository.CatMediaRepository;
import com.mousty00.chat_noir_api.repository.CatRepository;
import com.mousty00.chat_noir_api.repository.CatSubmissionRepository;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.util.PageDefaults;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatSubmissionService {

    private static final int DAILY_SUBMISSION_LIMIT = 3;
    private static final String SUBMISSION_MEDIA_PATH = "submissions/%s/%s.%s";

    private final CatSubmissionRepository submissionRepository;
    private final CatRepository catRepository;
    private final CatCategoryRepository catCategoryRepository;
    private final CatMediaRepository catMediaRepository;
    private final UserRepository userRepository;
    private final CatCategoryMapper catCategoryMapper;
    private final S3Service s3Service;
    private final MediaService mediaService;

    @Transactional
    public ApiResponse<CatSubmissionDTO> submitCat(CatSubmissionRequestDTO request) {
        UUID userId = resolveCurrentUserId();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayCount = submissionRepository.countByUserIdSince(userId, todayStart);
        if (todayCount >= DAILY_SUBMISSION_LIMIT) {
            throw CatSubmissionException.dailyLimitReached();
        }

        CatCategory category = resolveCategory(request.category());

        CatSubmission submission = CatSubmission.builder()
                .userId(userId)
                .name(request.name())
                .color(request.color())
                .category(category)
                .sourceName(request.sourceName())
                .notes(request.notes())
                .build();

        try {
            CatSubmission saved = submissionRepository.save(submission);
            return ApiResponse.success(HttpStatus.CREATED.value(), "Submission received successfully", toDTO(saved));
        } catch (Exception e) {
            throw CatSubmissionException.submissionSaveError(e);
        }
    }

    public ApiResponse<String> uploadSubmissionMedia(UUID submissionId, MultipartFile file) {
        mediaService.validateImageFile(file);

        CatSubmission submission = getOrThrow(submissionId);
        UUID currentUserId = resolveCurrentUserId();

        if (!submission.getUserId().equals(currentUserId)) {
            throw AuthenticationException.accessDenied();
        }
        if (submission.getStatus() != SubmissionStatus.PENDING) {
            throw CatSubmissionException.alreadyReviewed(submissionId);
        }

        String extension = mediaService.sanitizeExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
        String mediaKey = String.format(SUBMISSION_MEDIA_PATH, currentUserId, submissionId, extension);
        String oldMediaKey = submission.getMediaKey();

        try {
            s3Service.uploadFileAsync(file, mediaKey).get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload submission media", e);
        }

        if (oldMediaKey != null) {
            try { s3Service.deleteFile(oldMediaKey); } catch (Exception ignored) {}
        }

        persistSubmissionMediaKey(submissionId, mediaKey);

        return ApiResponse.success(HttpStatus.OK.value(), "Media uploaded successfully",
                s3Service.generatePresignedUrl(mediaKey));
    }

    @Transactional
    protected void persistSubmissionMediaKey(UUID submissionId, String mediaKey) {
        CatSubmission submission = getOrThrow(submissionId);
        submission.setMediaKey(mediaKey);
        submissionRepository.save(submission);
    }

    @Transactional
    public ApiResponse<PaginatedResponse<CatSubmissionDTO>> getMySubmissions(Integer page, Integer size) {
        UUID userId = resolveCurrentUserId();
        Pageable pageable = PageDefaults.of(page, size);
        Page<CatSubmission> result = submissionRepository.findByUserId(userId, pageable);
        return buildPageResponse(result, "Submissions retrieved successfully");
    }

    @Transactional
    public ApiResponse<PaginatedResponse<CatSubmissionDTO>> getPendingSubmissions(Integer page, Integer size) {
        Pageable pageable = PageDefaults.of(page, size);
        Page<CatSubmission> result = submissionRepository.findByStatus(SubmissionStatus.PENDING, pageable);
        return buildPageResponse(result, "Pending submissions retrieved successfully");
    }

    @Transactional
    @CacheEvict(value = "cats", allEntries = true)
    public ApiResponse<CatSubmissionDTO> approveSubmission(UUID submissionId) {
        CatSubmission submission = getOrThrow(submissionId);

        if (submission.getStatus() != SubmissionStatus.PENDING) {
            throw CatSubmissionException.alreadyReviewed(submissionId);
        }

        UUID adminId = resolveCurrentUserId();

        Cat cat = Cat.builder()
                .name(submission.getName())
                .color(submission.getColor())
                .category(submission.getCategory())
                .sourceName(submission.getSourceName())
                .build();
        Cat savedCat = catRepository.save(cat);

        if (submission.getMediaKey() != null) {
            CatMedia media = CatMedia.builder()
                    .cat(savedCat)
                    .mediaKey(submission.getMediaKey())
                    .mediaFormat(resolveMediaFormat(submission.getMediaKey()))
                    .contentUrl(s3Service.generatePresignedUrl(submission.getMediaKey()))
                    .build();
            CatMedia savedMedia = catMediaRepository.save(media);
            savedCat.setMedia(savedMedia);
            catRepository.save(savedCat);
        }

        submission.setStatus(SubmissionStatus.APPROVED);
        submission.setReviewedAt(LocalDateTime.now());
        submission.setReviewedBy(adminId);
        submissionRepository.save(submission);

        log.info("Submission {} approved by admin {}, cat {} created", submissionId, adminId, savedCat.getId());
        return ApiResponse.success(HttpStatus.OK.value(), "Submission approved", toDTO(submission));
    }

    @Transactional
    public ApiResponse<CatSubmissionDTO> rejectSubmission(UUID submissionId, String reason) {
        CatSubmission submission = getOrThrow(submissionId);

        if (submission.getStatus() != SubmissionStatus.PENDING) {
            throw CatSubmissionException.alreadyReviewed(submissionId);
        }

        UUID adminId = resolveCurrentUserId();

        submission.setStatus(SubmissionStatus.REJECTED);
        submission.setReviewedAt(LocalDateTime.now());
        submission.setReviewedBy(adminId);
        submission.setRejectionReason(reason);
        submissionRepository.save(submission);

        log.info("Submission {} rejected by admin {}", submissionId, adminId);
        return ApiResponse.success(HttpStatus.OK.value(), "Submission rejected", toDTO(submission));
    }

    private CatSubmission getOrThrow(UUID id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> CatSubmissionException.submissionNotFound(id));
    }

    private CatCategory resolveCategory(CatCategoryDTO dto) {
        if (dto == null || dto.id() == null) {
            throw CatException.categoryRequired();
        }
        return catCategoryRepository.findById(dto.id())
                .orElseThrow(() -> CatException.categoryNotFound(dto.id()));
    }

    private UUID resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = Objects.requireNonNull(auth).getName();
        return userRepository.findByUsername(username)
                .orElseThrow(AuthenticationException::accessDenied)
                .getId();
    }

    private String resolveMediaFormat(String mediaKey) {
        String ext = FilenameUtils.getExtension(mediaKey);
        return switch (ext.toLowerCase()) {
            case "png"  -> "image/png";
            case "gif"  -> "image/gif";
            case "webp" -> "image/webp";
            default     -> "image/jpeg";
        };
    }

    private ApiResponse<PaginatedResponse<CatSubmissionDTO>> buildPageResponse(Page<CatSubmission> page, String message) {
        PaginatedResponse<CatSubmissionDTO> paginated = new PaginatedResponse<>(
                page.getContent().stream().map(this::toDTO).toList(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
        return ApiResponse.success(HttpStatus.OK.value(), message, paginated);
    }

    private CatSubmissionDTO toDTO(CatSubmission s) {
        return CatSubmissionDTO.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .name(s.getName())
                .color(s.getColor())
                .category(catCategoryMapper.toDTO(s.getCategory()))
                .sourceName(s.getSourceName())
                .notes(s.getNotes())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .reviewedAt(s.getReviewedAt())
                .reviewedBy(s.getReviewedBy())
                .rejectionReason(s.getRejectionReason())
                .build();
    }
}
