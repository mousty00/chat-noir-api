package com.mousty00.chat_noir_api.dto.user;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record SubscriptionPlanDTO(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer durationDays,
        String stripePriceId
) {
}
