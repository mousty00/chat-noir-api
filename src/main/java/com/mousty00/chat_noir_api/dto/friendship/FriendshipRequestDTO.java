package com.mousty00.chat_noir_api.dto.friendship;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FriendshipRequestDTO(
        @NotNull UUID addresseeId
) {
}
