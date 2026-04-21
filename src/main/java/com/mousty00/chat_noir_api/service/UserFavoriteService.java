package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.user.UserFavoriteDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatMedia;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserFavorite;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.exception.UserException;
import com.mousty00.chat_noir_api.exception.UserFavoriteException;
import com.mousty00.chat_noir_api.mapper.CatCategoryMapper;
import com.mousty00.chat_noir_api.repository.CatMediaRepository;
import com.mousty00.chat_noir_api.repository.CatRepository;
import com.mousty00.chat_noir_api.repository.UserFavoriteRepository;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.util.PageDefaults;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;
    private final UserRepository userRepository;
    private final CatMediaRepository catMediaRepository;
    private final CatRepository catRepository;
    private final CatCategoryMapper catCategoryMapper;
    private final S3Service s3Service;

    @Transactional
    public ApiResponse<PaginatedResponse<UserFavoriteDTO>> getUserFavorites(Integer page, Integer size) {
        UUID userId = resolveCurrentUserId();
        Pageable pageable = PageDefaults.of(page, size);

        Page<UserFavorite> favoritesPage = userFavoriteRepository.findByUserId(userId, pageable);

        List<UUID> mediaIds = favoritesPage.getContent().stream()
                .map(UserFavorite::getCatMediaId)
                .toList();

        List<CatMedia> mediaList = catMediaRepository.findAllById(mediaIds);
        List<UUID> catIds = mediaList.stream()
                .map(media -> media.getCat().getId())
                .distinct()
                .toList();
        List<Cat> cats = catRepository.findAllById(catIds);

        Map<UUID, Cat> catById = cats.stream()
                .collect(Collectors.toMap(Cat::getId, cat -> cat));

        Map<UUID, CatMedia> mediaById = mediaList.stream()
                .collect(Collectors.toMap(CatMedia::getId, media -> media));

        List<UserFavoriteDTO> dtoList = favoritesPage.getContent().stream()
                .map(fav -> {
                    CatMedia media = mediaById.get(fav.getCatMediaId());
                    Cat cat = catById.get(media.getCat().getId());
                    return toDTO(fav, media, cat);
                })
                .toList();

        PaginatedResponse<UserFavoriteDTO> paginated = new PaginatedResponse<>(
                dtoList,
                favoritesPage.getNumber(),
                favoritesPage.getTotalPages(),
                favoritesPage.getTotalElements(),
                favoritesPage.getSize(),
                favoritesPage.hasNext(),
                favoritesPage.hasPrevious()
        );

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Favorites retrieved successfully",
                paginated
        );
    }

    @Transactional
    public ApiResponse<PaginatedResponse<UserFavoriteDTO>> getUserFavoritesForUser(UUID userId, Pageable pageable) {
        Page<UserFavorite> favoritesPage = userFavoriteRepository.findByUserId(userId, pageable);

        List<UUID> mediaIds = favoritesPage.getContent().stream()
                .map(UserFavorite::getCatMediaId)
                .toList();

        List<CatMedia> mediaList = catMediaRepository.findAllById(mediaIds);
        List<UUID> catIds = mediaList.stream()
                .map(media -> media.getCat().getId())
                .distinct()
                .toList();
        List<Cat> cats = catRepository.findAllById(catIds);

        Map<UUID, Cat> catById = cats.stream()
                .collect(Collectors.toMap(Cat::getId, cat -> cat));

        Map<UUID, CatMedia> mediaById = mediaList.stream()
                .collect(Collectors.toMap(CatMedia::getId, media -> media));

        List<UserFavoriteDTO> dtoList = favoritesPage.getContent().stream()
                .map(fav -> {
                    CatMedia media = mediaById.get(fav.getCatMediaId());
                    Cat cat = catById.get(media.getCat().getId());
                    return toDTO(fav, media, cat);
                })
                .toList();

        PaginatedResponse<UserFavoriteDTO> paginated = new PaginatedResponse<>(
                dtoList,
                favoritesPage.getNumber(),
                favoritesPage.getTotalPages(),
                favoritesPage.getTotalElements(),
                favoritesPage.getSize(),
                favoritesPage.hasNext(),
                favoritesPage.hasPrevious()
        );

        return ApiResponse.success(HttpStatus.OK.value(), "Favorites retrieved successfully", paginated);
    }

    @Transactional
    public ApiResponse<UserFavoriteDTO> addFavorite(UUID catId) {
        UUID userId = resolveCurrentUserId();
        Cat cat = catRepository.findById(catId)
                .orElseThrow(() -> CatException.catNotFound(catId));

        if (userFavoriteRepository.existsByUserIdAndCatId(userId, catId)) {
            throw UserFavoriteException.alreadyFavoritedCat(catId);
        }

        CatMedia media = catMediaRepository.findByCatId(catId)
                .orElseThrow(() -> CatException.catMediaNotFound(catId));

        try {
            UserFavorite saved = userFavoriteRepository.save(
                    UserFavorite.builder()
                            .userId(userId)
                            .catMediaId(media.getId())
                            .build()
            );
            return ApiResponse.success(HttpStatus.CREATED.value(),
                    "Favorite added successfully",
                    toDTO(saved, media, cat));
        } catch (Exception e) {
            throw UserFavoriteException.favoriteSaveError(e);
        }
    }

    @Transactional
    public ApiResponse<?> removeFavorite(UUID catId) {
        UUID userId = resolveCurrentUserId();

        UserFavorite favorite = userFavoriteRepository.findByUserIdAndCatId(userId, catId)
                .orElseThrow(() -> UserFavoriteException.favoriteNotFoundForCat(catId));

        try {
            userFavoriteRepository.delete(favorite);
            return ApiResponse.success(HttpStatus.OK.value(),
                    "Favorite removed successfully", null);
        } catch (Exception e) {
            throw UserFavoriteException.favoriteDeleteError(e);
        }
    }

    public ApiResponse<Boolean> isFavorite(UUID catId) {
        UUID userId = resolveCurrentUserId();
        boolean result = userFavoriteRepository.existsByUserIdAndCatId(userId, catId);
        return ApiResponse.success(HttpStatus.OK.value(), "Check completed", result);
    }

    private UUID resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = Objects.requireNonNull(auth).getName();
        return resolveUserIdByUsername(username);
    }

    @Cacheable(value = "userIds", key = "#username")
    public UUID resolveUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(UserException::userNotFound)
                .getId();
    }

    private UserFavoriteDTO toDTO(UserFavorite favorite, CatMedia media, Cat cat) {

        String imageUrl = s3Service.generatePresignedUrl(media.getMediaKey());

        return UserFavoriteDTO.builder()
                .id(favorite.getId())
                .userId(favorite.getUserId())
                .catMediaId(favorite.getCatMediaId())
                .cat(CatDTO.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .color(cat.getColor())
                        .image(imageUrl)
                        .category(catCategoryMapper.toDTO(cat.getCategory()))
                        .build())
                .build();
    }
}
