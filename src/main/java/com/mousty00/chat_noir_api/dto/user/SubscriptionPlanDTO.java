package com.mousty00.chat_noir_api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private String stripePriceId;
}