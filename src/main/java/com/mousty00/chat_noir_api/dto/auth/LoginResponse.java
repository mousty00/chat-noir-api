package com.mousty00.chat_noir_api.dto.auth;

import lombok.Builder;

import java.util.List;

@Builder
public record LoginResponse(
        String token,
        String username,
        String email,
        boolean isAdmin,
        List<String> roles
) {
}
