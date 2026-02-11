package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.LoginRequest;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.dto.auth.RegisterRequest;
import com.mousty00.chat_noir_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @MutationMapping
    public ApiResponse<LoginResponse> login(@Argument LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ApiResponse.<LoginResponse>builder()
                .status(200)
                .error(false)
                .success(true)
                .data(response)
                .build();
        } catch (Exception e) {
            return ApiResponse.<LoginResponse>builder()
                .status(401)
                .error(true)
                .success(false)
                .message("Invalid credentials")
                .build();
        }
    }
    
    @MutationMapping
    public ApiResponse<String> register(@Argument RegisterRequest request) {
        try {
            authService.register(request);
            return ApiResponse.<String>builder()
                .status(201)
                .error(false)
                .success(true)
                .message("User registered successfully")
                .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                .status(400)
                .error(true)
                .success(false)
                .message(e.getMessage())
                .build();
        }
    }
}