package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.auth.ForgotPasswordRequest;
import com.mousty00.chat_noir_api.dto.auth.LoginRequest;
import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.dto.auth.RegisterRequest;
import com.mousty00.chat_noir_api.dto.auth.ResetPasswordRequest;
import com.mousty00.chat_noir_api.exception.ApiException;
import com.mousty00.chat_noir_api.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthRestController {

    private final AuthService service;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${frontend.domain:localhost:3000}")
    private String feDomain;

    @Value("${app.email-verification.redirect-path:/verify-email}")
    private String verificationRedirectPath;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody @Valid RegisterRequest request) {
        return service.register(request);
    }

    @GetMapping("/verify-email")
    public void verifyEmail(@RequestParam String token, HttpServletResponse response) throws IOException {
        try {
            service.verifyEmail(token);
            response.sendRedirect(buildVerificationRedirectUrl(true, "Email verified successfully"));
        } catch (ApiException ex) {
            logger.error("Failed to verify email", ex);
            response.sendRedirect(buildVerificationRedirectUrl(false, ex.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ApiResponse<String> resendVerification(@RequestBody @Valid ForgotPasswordRequest request) {
        return service.resendVerificationEmail(request);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        return service.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return service.resetPassword(request);
    }

    private String buildVerificationRedirectUrl(boolean success, String message) {
        boolean hasPort = feDomain.contains(":");
        String frontendBaseUrl = hasPort ? "http://" + feDomain : "https://" + feDomain;

        return ServletUriComponentsBuilder.fromUriString(frontendBaseUrl)
                .path(verificationRedirectPath)
                .queryParam("success", success)
                .queryParam("message", message)
                .build()
                .toUriString();
    }
}
