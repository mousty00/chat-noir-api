package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.auth.LoginRequest;
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
        return authService.login(request);
    }
    
    @MutationMapping
    public ApiResponse<String> register(@Argument RegisterRequest request) {
        return authService.register(request);
    }
}