package com.mousty00.chat_noir_api.dto.user.role;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserRoleDTO(
        UUID id,
        String name,
        String description
) {
}
