package com.mousty00.chat_noir_api.dto.user;

import com.mousty00.chat_noir_api.entity.FriendshipStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserPublicDTO(
        UUID id,
        String username,
        String image,
        FriendshipStatus friendStatus
) {
}
