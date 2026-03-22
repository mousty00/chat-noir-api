package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.auth.LoginRequest;
import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.dto.auth.RegisterRequest;
import com.mousty00.chat_noir_api.exception.ApiException;
import com.mousty00.chat_noir_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @MutationMapping
    public ApiResponse<LoginResponse> login(@Argument LoginRequest request) {
        try {
            return authService.login(request);
        } catch (ApiException e) {
            return ApiResponse.error(e.getStatus().value(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            return ApiResponse.internalError("An unexpected error occurred");
        }
    }

    @MutationMapping
    public ApiResponse<String> register(@Argument RegisterRequest request) {
        try {
            return authService.register(request);
        } catch (ApiException e) {
            return ApiResponse.error(e.getStatus().value(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during register", e);
            return ApiResponse.internalError("An unexpected error occurred");
        }
    }
}