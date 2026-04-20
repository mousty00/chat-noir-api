package com.mousty00.chat_noir_api.dto.auth;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record LoginResponse(
        String token,
        UUID id,
        String username,
        String email,
        boolean isAdmin,
        List<String> roles,
        String image
) {
}
