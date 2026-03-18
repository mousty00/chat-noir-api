package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.user.UserFavoriteDTO;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserFavorite;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.exception.UserException;
import com.mousty00.chat_noir_api.exception.UserFavoriteException;
import com.mousty00.chat_noir_api.repository.CatMediaRepository;
import com.mousty00.chat_noir_api.repository.UserFavoriteRepository;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.util.PageDefaults;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;
    private final UserRepository userRepository;
    private final CatMediaRepository catMediaRepository;

    public ApiResponse<PaginatedResponse<UserFavoriteDTO>> getUserFavorites(Integer page, Integer size) {
        UUID userId = resolveCurrentUserId();
        Pageable pageable = PageDefaults.of(page, size);
        Page<UserFavoriteDTO> pageResult = userFavoriteRepository
                .findByUserId(userId, pageable)
                .map(this::toDTO);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Favorites retrieved successfully",
                buildPaginatedResponse(pageResult)
        );
    }

    @Transactional
    public ApiResponse<UserFavoriteDTO> addFavorite(UUID catMediaId) {
        UUID userId = resolveCurrentUserId();

        catMediaRepository.findById(catMediaId)
                .orElseThrow(() -> CatException.catMediaNotFound(catMediaId));

        if (userFavoriteRepository.existsByUserIdAndCatMediaId(userId, catMediaId)) {
            throw UserFavoriteException.alreadyFavorited(catMediaId);
        }

        try {
            UserFavorite saved = userFavoriteRepository.save(
                    UserFavorite.builder()
                            .userId(userId)
                            .catMediaId(catMediaId)
                            .build()
            );
            return ApiResponse.success(HttpStatus.CREATED.value(), "Favorite added successfully", toDTO(saved));
        } catch (Exception e) {
            throw UserFavoriteException.favoriteSaveError(e);
        }
    }

    @Transactional
    public ApiResponse<?> removeFavorite(UUID catMediaId) {
        UUID userId = resolveCurrentUserId();

        userFavoriteRepository.findByUserIdAndCatMediaId(userId, catMediaId)
                .orElseThrow(() -> UserFavoriteException.favoriteNotFound(catMediaId));

        try {
            userFavoriteRepository.deleteByUserIdAndCatMediaId(userId, catMediaId);
            return ApiResponse.success(HttpStatus.OK.value(), "Favorite removed successfully", null);
        } catch (Exception e) {
            throw UserFavoriteException.favoriteDeleteError(e);
        }
    }

    public ApiResponse<Boolean> isFavorite(UUID catMediaId) {
        UUID userId = resolveCurrentUserId();
        boolean result = userFavoriteRepository.existsByUserIdAndCatMediaId(userId, catMediaId);
        return ApiResponse.success(HttpStatus.OK.value(), "Check completed", result);
    }

    private UUID resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = Objects.requireNonNull(auth).getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserException::userNotFound);
        return user.getId();
    }

    private UserFavoriteDTO toDTO(UserFavorite favorite) {
        return UserFavoriteDTO.builder()
                .id(favorite.getId())
                .userId(favorite.getUserId())
                .catMediaId(favorite.getCatMediaId())
                .build();
    }

    private PaginatedResponse<UserFavoriteDTO> buildPaginatedResponse(Page<UserFavoriteDTO> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
