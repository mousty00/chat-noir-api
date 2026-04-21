package com.mousty00.chat_noir_api.dto.friendship;

import com.mousty00.chat_noir_api.dto.user.UserPublicDTO;
import com.mousty00.chat_noir_api.entity.FriendshipStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record FriendshipDTO(
        UUID id,
        UserPublicDTO friend,
        FriendshipStatus status,
        boolean iAmRequester,
        Instant createdAt
) {
}
