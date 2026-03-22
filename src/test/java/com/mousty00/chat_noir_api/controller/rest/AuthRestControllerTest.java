package com.mousty00.chat_noir_api.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.auth.LoginRequest;
import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.dto.auth.RegisterRequest;
import com.mousty00.chat_noir_api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthRestController")
class AuthRestControllerTest {

    @Mock AuthService authService;
    @InjectMocks AuthRestController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /auth/login returns 200 with token on valid credentials")
    void login_validCredentials_returns200() throws Exception {
        LoginResponse loginResponse = LoginResponse.builder()
                .token("jwt-token").username("testuser")
                .email("test@example.com").isAdmin(false)
                .roles(List.of("USER")).build();
        ApiResponse<LoginResponse> apiResponse = ApiResponse.success(200, "Login successful", loginResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(apiResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("testuser", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /auth/login returns error response on bad credentials")
    void login_badCredentials_returnsErrorResponse() throws Exception {
        ApiResponse<LoginResponse> errorResponse = ApiResponse.error(401, "Invalid username or password");
        when(authService.login(any(LoginRequest.class))).thenReturn(errorResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("wrong", "wrong"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("POST /auth/register returns 200 on successful registration")
    void register_newUser_returns200() throws Exception {
        ApiResponse<String> apiResponse = ApiResponse.success(201, "User registered successfully", "");
        when(authService.register(any(RegisterRequest.class))).thenReturn(apiResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("newuser", "new@example.com", "securePass1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /auth/register returns conflict response for duplicate username")
    void register_duplicateUsername_returnsConflict() throws Exception {
        ApiResponse<String> errorResponse = ApiResponse.error(409, "Username already exists");
        when(authService.register(any(RegisterRequest.class))).thenReturn(errorResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest("existing", "e@example.com", "pass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }
}
