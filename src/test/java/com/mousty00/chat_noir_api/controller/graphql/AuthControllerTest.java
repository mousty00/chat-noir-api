package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.auth.LoginRequest;
import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.dto.auth.RegisterRequest;
import com.mousty00.chat_noir_api.exception.AuthenticationException;
import com.mousty00.chat_noir_api.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController (GraphQL)")
class AuthControllerTest {

    @Mock AuthService authService;
    @InjectMocks AuthController controller;

    @Nested
    @DisplayName("login mutation")
    class Login {

        @Test
        @DisplayName("delegates to authService and returns success response")
        void login_success_returnsApiResponse() {
            LoginResponse loginResponse = LoginResponse.builder()
                    .token("jwt").username("user").email("u@e.com")
                    .isAdmin(false).roles(List.of("USER")).build();
            ApiResponse<LoginResponse> expected = ApiResponse.success(200, "Login successful", loginResponse);
            when(authService.login(any(LoginRequest.class))).thenReturn(expected);

            ApiResponse<LoginResponse> result = controller.login(new LoginRequest("user", "pass"));

            assertThat(result.success()).isTrue();
            assertThat(result.data().token()).isEqualTo("jwt");
        }

        @Test
        @DisplayName("catches ApiException and returns error response")
        void login_authException_returnsErrorResponse() {
            when(authService.login(any())).thenThrow(AuthenticationException.badCredentials());

            ApiResponse<LoginResponse> result = controller.login(new LoginRequest("wrong", "wrong"));

            assertThat(result.success()).isFalse();
            assertThat(result.message()).isEqualTo("Invalid username or password");
        }

        @Test
        @DisplayName("catches unexpected exception and returns internal error response")
        void login_unexpectedException_returnsInternalError() {
            when(authService.login(any())).thenThrow(new RuntimeException("Unexpected"));

            ApiResponse<LoginResponse> result = controller.login(new LoginRequest("user", "pass"));

            assertThat(result.success()).isFalse();
            assertThat(result.status()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("register mutation")
    class Register {

        @Test
        @DisplayName("delegates to authService and returns success response")
        void register_success_returnsApiResponse() {
            ApiResponse<String> expected = ApiResponse.success(201, "User registered successfully", "");
            when(authService.register(any(RegisterRequest.class))).thenReturn(expected);

            ApiResponse<String> result = controller.register(new RegisterRequest("user", "u@e.com", "pass"));

            assertThat(result.success()).isTrue();
            assertThat(result.status()).isEqualTo(201);
        }

        @Test
        @DisplayName("catches ApiException and returns error response")
        void register_apiException_returnsErrorResponse() {
            when(authService.register(any())).thenThrow(AuthenticationException.badCredentials());

            ApiResponse<String> result = controller.register(new RegisterRequest("u", "e@e.com", "p"));

            assertThat(result.success()).isFalse();
        }
    }
}
