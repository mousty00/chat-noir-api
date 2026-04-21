package com.mousty00.chat_noir_api.dto.auth;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record LoginResponse(
        UUID id,
        String token,
        String username,
        String email,
        String image,
        boolean isAdmin,
        List<String> roles
) {
}
