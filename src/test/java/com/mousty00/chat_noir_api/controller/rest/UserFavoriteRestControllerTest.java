package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.user.UserFavoriteDTO;
import com.mousty00.chat_noir_api.service.UserFavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFavoriteRestController")
class UserFavoriteRestControllerTest {

    @Mock UserFavoriteService userFavoriteService;
    @InjectMocks UserFavoriteRestController controller;

    MockMvc mockMvc;
    UUID catId;
    UserFavoriteDTO favoriteDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        catId = UUID.randomUUID();
        favoriteDTO = UserFavoriteDTO.builder()
                .id(UUID.randomUUID()).userId(UUID.randomUUID())
                .catMediaId(UUID.randomUUID()).build();
    }

    @Test
    @DisplayName("GET /favorites returns paginated favorites")
    void getMyFavorites_returnsPaginatedList() throws Exception {
        PaginatedResponse<UserFavoriteDTO> paginated = PaginatedResponse.<UserFavoriteDTO>builder()
                .result(List.of(favoriteDTO)).currentPage(0).totalPages(1)
                .totalItems(1).pageSize(12).hasNext(false).hasPrevious(false).build();
        when(userFavoriteService.getUserFavorites(any(), any()))
                .thenReturn(ApiResponse.success(200, "Favorites retrieved successfully", paginated));

        mockMvc.perform(get("/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.result").isArray())
                .andExpect(jsonPath("$.data.totalItems").value(1));
    }

    @Test
    @DisplayName("POST /favorites/{catId} adds favorite and returns DTO")
    void addFavorite_returnsCreatedFavorite() throws Exception {
        when(userFavoriteService.addFavorite(catId))
                .thenReturn(ApiResponse.success(201, "Favorite added successfully", favoriteDTO));

        mockMvc.perform(post("/favorites/{catId}", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userFavoriteService).addFavorite(catId);
    }

    @Test
    @DisplayName("DELETE /favorites/{catId} removes favorite and returns success")
    void removeFavorite_returnsSuccess() throws Exception {
        when(userFavoriteService.removeFavorite(catId))
                .thenReturn(ApiResponse.success(200, "Favorite removed successfully", null));

        mockMvc.perform(delete("/favorites/{catId}", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userFavoriteService).removeFavorite(catId);
    }

    @Test
    @DisplayName("GET /favorites/{catId}/check returns true when favorited")
    void isFavorite_true_returnsTrue() throws Exception {
        when(userFavoriteService.isFavorite(catId))
                .thenReturn(ApiResponse.success(200, "Check completed", true));

        mockMvc.perform(get("/favorites/{catId}/check", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("GET /favorites/{catId}/check returns false when not favorited")
    void isFavorite_false_returnsFalse() throws Exception {
        when(userFavoriteService.isFavorite(catId))
                .thenReturn(ApiResponse.success(200, "Check completed", false));

        mockMvc.perform(get("/favorites/{catId}/check", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }
}
