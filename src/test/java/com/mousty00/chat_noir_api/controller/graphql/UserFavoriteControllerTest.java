package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.user.UserFavoriteDTO;
import com.mousty00.chat_noir_api.service.UserFavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFavoriteController (GraphQL)")
class UserFavoriteControllerTest {

    @Mock UserFavoriteService service;
    @InjectMocks UserFavoriteController controller;

    UUID catId;
    UserFavoriteDTO favoriteDTO;

    @BeforeEach
    void setUp() {
        catId = UUID.randomUUID();
        favoriteDTO = UserFavoriteDTO.builder()
                .id(UUID.randomUUID()).userId(UUID.randomUUID())
                .catMediaId(UUID.randomUUID()).build();
    }

    @Nested
    @DisplayName("myFavorites query")
    class MyFavorites {

        @Test
        @DisplayName("returns paginated favorites for current user")
        void myFavorites_returnsPaginatedResponse() {
            PaginatedResponse<UserFavoriteDTO> paginated = PaginatedResponse.<UserFavoriteDTO>builder()
                    .result(List.of(favoriteDTO)).currentPage(0).totalPages(1)
                    .totalItems(1).pageSize(12).hasNext(false).hasPrevious(false).build();
            when(service.getUserFavorites(0, 12))
                    .thenReturn(ApiResponse.success(200, "Favorites retrieved successfully", paginated));

            ApiResponse<PaginatedResponse<UserFavoriteDTO>> result = controller.myFavorites(0, 12);

            assertThat(result.success()).isTrue();
            assertThat(result.data().result()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("isFavorite query")
    class IsFavorite {

        @Test
        @DisplayName("returns true when cat is favorited")
        void isFavorite_favorited_returnsTrue() {
            when(service.isFavorite(catId)).thenReturn(ApiResponse.success(200, "Check completed", true));

            ApiResponse<Boolean> result = controller.isFavorite(catId);

            assertThat(result.data()).isTrue();
        }

        @Test
        @DisplayName("returns false when cat is not favorited")
        void isFavorite_notFavorited_returnsFalse() {
            when(service.isFavorite(catId)).thenReturn(ApiResponse.success(200, "Check completed", false));

            ApiResponse<Boolean> result = controller.isFavorite(catId);

            assertThat(result.data()).isFalse();
        }
    }

    @Nested
    @DisplayName("addFavorite mutation")
    class AddFavorite {

        @Test
        @DisplayName("delegates to service and returns created favorite")
        void addFavorite_success() {
            when(service.addFavorite(catId))
                    .thenReturn(ApiResponse.success(201, "Favorite added successfully", favoriteDTO));

            ApiResponse<UserFavoriteDTO> result = controller.addFavorite(catId);

            assertThat(result.success()).isTrue();
            verify(service).addFavorite(catId);
        }
    }

    @Nested
    @DisplayName("removeFavorite mutation")
    class RemoveFavorite {

        @Test
        @DisplayName("delegates to service and returns success")
        void removeFavorite_success() {
            when(service.removeFavorite(catId))
                    .thenReturn(ApiResponse.success(200, "Favorite removed successfully", null));

            ApiResponse<?> result = controller.removeFavorite(catId);

            assertThat(result.success()).isTrue();
            verify(service).removeFavorite(catId);
        }
    }
}
