package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.LoginRequest;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.dto.auth.RegisterRequest;
import com.mousty00.chat_noir_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthRestController {

    private final AuthService service;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestParam LoginRequest request) {
       return service.login(request);
    }

    @PostMapping
    public ApiResponse<String> register(@RequestParam RegisterRequest request) {
        return service.register(request);
    }
}
