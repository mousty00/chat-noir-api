package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.dto.user.UserFavoriteDTO;
import com.mousty00.chat_noir_api.entity.*;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.exception.UserFavoriteException;
import com.mousty00.chat_noir_api.mapper.CatCategoryMapper;
import com.mousty00.chat_noir_api.repository.CatMediaRepository;
import com.mousty00.chat_noir_api.repository.CatRepository;
import com.mousty00.chat_noir_api.repository.UserFavoriteRepository;
import com.mousty00.chat_noir_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFavoriteService")
class UserFavoriteServiceTest {

    @Mock UserFavoriteRepository userFavoriteRepository;
    @Mock UserRepository userRepository;
    @Mock CatMediaRepository catMediaRepository;
    @Mock CatRepository catRepository;
    @Mock CatCategoryMapper catCategoryMapper;
    @Mock S3Service s3Service;

    @InjectMocks UserFavoriteService service;

    UUID userId;
    UUID catId;
    UUID mediaId;
    User user;
    Cat cat;
    CatMedia catMedia;
    UserFavorite userFavorite;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        catId = UUID.randomUUID();
        mediaId = UUID.randomUUID();

        UserRole role = UserRole.builder().id(UUID.randomUUID()).name("USER").build();
        CatCategory category = CatCategory.builder().id(UUID.randomUUID()).name("Cyberpunk").mediaTypeHint("image").build();

        user = User.builder()
                .id(userId).username("testuser")
                .email("test@example.com").password("pw")
                .isAdmin(false).createdAt(Instant.now()).role(role)
                .build();

        cat = Cat.builder().id(catId).name("Shadow").color("Black").category(category).build();

        catMedia = CatMedia.builder()
                .id(mediaId).cat(cat).mediaKey("key/cat.jpg").mediaFormat("image/jpeg")
                .build();

        userFavorite = UserFavorite.builder()
                .id(UUID.randomUUID()).userId(userId).catMediaId(mediaId)
                .build();

        var auth = new UsernamePasswordAuthenticationToken("testuser", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getUserFavorites")
    class GetFavorites {

        @Test
        @DisplayName("returns paginated favorites for current user")
        void getFavorites_returnsPaginatedResponse() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(userFavoriteRepository.findByUserId(eq(userId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(userFavorite)));
            when(catMediaRepository.findAllById(List.of(mediaId))).thenReturn(List.of(catMedia));
            when(catRepository.findAllById(List.of(catId))).thenReturn(List.of(cat));
            when(s3Service.generatePresignedUrl(catMedia.getMediaKey())).thenReturn("https://s3/image.jpg");
            when(catCategoryMapper.toDTO(cat.getCategory())).thenReturn(
                    CatCategoryDTO.builder().id(cat.getCategory().getId()).name("Cyberpunk").build());

            ApiResponse<PaginatedResponse<UserFavoriteDTO>> response = service.getUserFavorites(0, 12);

            assertThat(response.success()).isTrue();
            assertThat(response.data().result()).hasSize(1);
            assertThat(response.data().result().get(0).userId()).isEqualTo(userId);
        }
    }

    @Nested
    @DisplayName("addFavorite")
    class AddFavorite {

        @Test
        @DisplayName("adds favorite and returns success response")
        void addFavorite_success() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));
            when(userFavoriteRepository.existsByUserIdAndCatId(userId, catId)).thenReturn(false);
            when(catMediaRepository.findByCatId(catId)).thenReturn(Optional.of(catMedia));
            when(userFavoriteRepository.save(any())).thenReturn(userFavorite);
            when(s3Service.generatePresignedUrl(catMedia.getMediaKey())).thenReturn("https://s3/image.jpg");
            when(catCategoryMapper.toDTO(cat.getCategory())).thenReturn(
                    CatCategoryDTO.builder().id(cat.getCategory().getId()).name("Cyberpunk").build());

            ApiResponse<UserFavoriteDTO> response = service.addFavorite(catId);

            assertThat(response.success()).isTrue();
            verify(userFavoriteRepository).save(any());
        }

        @Test
        @DisplayName("throws exception when cat not found")
        void addFavorite_catNotFound_throwsCatException() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(catRepository.findById(catId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addFavorite(catId))
                    .isInstanceOf(CatException.class);
        }

        @Test
        @DisplayName("throws exception when already favorited")
        void addFavorite_alreadyFavorited_throwsException() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));
            when(userFavoriteRepository.existsByUserIdAndCatId(userId, catId)).thenReturn(true);

            assertThatThrownBy(() -> service.addFavorite(catId))
                    .isInstanceOf(UserFavoriteException.class);
        }

        @Test
        @DisplayName("throws exception when cat has no media")
        void addFavorite_noMedia_throwsCatException() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));
            when(userFavoriteRepository.existsByUserIdAndCatId(userId, catId)).thenReturn(false);
            when(catMediaRepository.findByCatId(catId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addFavorite(catId))
                    .isInstanceOf(CatException.class);
        }
    }

    @Nested
    @DisplayName("removeFavorite")
    class RemoveFavorite {

        @Test
        @DisplayName("removes favorite and returns success")
        void removeFavorite_success() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(userFavoriteRepository.findByUserIdAndCatId(userId, catId)).thenReturn(Optional.of(userFavorite));
            doNothing().when(userFavoriteRepository).delete(userFavorite);

            ApiResponse<?> response = service.removeFavorite(catId);

            assertThat(response.success()).isTrue();
            verify(userFavoriteRepository).delete(userFavorite);
        }

        @Test
        @DisplayName("throws exception when favorite not found")
        void removeFavorite_notFound_throwsException() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(userFavoriteRepository.findByUserIdAndCatId(userId, catId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.removeFavorite(catId))
                    .isInstanceOf(UserFavoriteException.class);
        }
    }

    @Nested
    @DisplayName("isFavorite")
    class IsFavorite {

        @Test
        @DisplayName("returns true when cat is favorited")
        void isFavorite_true() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(userFavoriteRepository.existsByUserIdAndCatId(userId, catId)).thenReturn(true);

            ApiResponse<Boolean> response = service.isFavorite(catId);

            assertThat(response.success()).isTrue();
            assertThat(response.data()).isTrue();
        }

        @Test
        @DisplayName("returns false when cat is not favorited")
        void isFavorite_false() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(userFavoriteRepository.existsByUserIdAndCatId(userId, catId)).thenReturn(false);

            ApiResponse<Boolean> response = service.isFavorite(catId);

            assertThat(response.data()).isFalse();
        }
    }
}
