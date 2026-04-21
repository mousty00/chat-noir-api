package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.user.UserDTO;
import com.mousty00.chat_noir_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserRestController")
class UserRestControllerTest {

    @Mock UserService userService;
    @InjectMocks UserRestController controller;

    MockMvc mockMvc;
    UUID userId;
    UserDTO userDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        userId = UUID.randomUUID();
        userDTO = mock(UserDTO.class);
        when(userDTO.id()).thenReturn(userId);
    }

    private static UserDTO mock(Class<UserDTO> clazz) {
        return org.mockito.Mockito.mock(clazz);
    }

    @Test
    @DisplayName("GET /users returns paginated users")
    void getUsers_returnsPaginatedList() throws Exception {
        PaginatedResponse<UserDTO> paginated = PaginatedResponse.<UserDTO>builder()
                .result(List.of(userDTO)).currentPage(0).totalPages(1)
                .totalItems(1).pageSize(10).hasNext(false).hasPrevious(false).build();
        when(userService.getUsers(any(), any(), any())).thenReturn(ApiResponse.success(200, "OK", paginated));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.result").isArray());
    }

    @Test
    @DisplayName("GET /users/{id} returns user by id")
    void getUserById_found_returnsUser() throws Exception {
        when(userService.getUserById(userId)).thenReturn(ApiResponse.success(200, "OK", userDTO));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /users/{id} deletes user and returns success")
    void deleteUser_success() throws Exception {
        when(userService.deleteUser(userId)).thenReturn(ApiResponse.success(200, "deleted", null));

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userService).deleteUser(userId);
    }

    @Test
    @DisplayName("POST /users/me/profile-image uploads image and returns URL")
    void uploadProfileImage_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("imageFile", "profile.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3});
        when(userService.uploadProfileImage(any())).thenReturn(ApiResponse.success(200, "OK", "https://s3/presigned"));

        mockMvc.perform(multipart("/users/me/profile-image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("https://s3/presigned"));
    }
}
