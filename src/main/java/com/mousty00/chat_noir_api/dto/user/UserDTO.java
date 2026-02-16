package com.mousty00.chat_noir_api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private Instant subscriptionStartDate;
    private Instant subscriptionEndDate;
    private Instant createdAt;
    private String image;
}
