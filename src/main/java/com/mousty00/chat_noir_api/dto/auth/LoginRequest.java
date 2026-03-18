package com.mousty00.chat_noir_api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
