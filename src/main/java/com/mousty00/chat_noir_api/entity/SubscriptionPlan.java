package com.mousty00.chat_noir_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "subscription_plan")
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {
    @Id
    @GeneratedValue(generator = "UUID")
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "yearly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal yearlyPrice;

    @ColumnDefault("0")
    @Column(name = "api_rate_limit", nullable = false)
    private Integer apiRateLimit;

    @ColumnDefault("20")
    @Column(name = "max_favorites", nullable = false)
    private Integer maxFavorites;

    @ColumnDefault("true")
    @Column(name = "access_to_gif", nullable = false)
    private Boolean accessToGif = false;

    @ColumnDefault("true")
    @Column(name = "access_to_memes", nullable = false)
    private Boolean accessToMemes = false;

    @ColumnDefault("false")
    @Column(name = "access_to_wallpaper", nullable = false)
    private Boolean accessToWallpaper = false;

    @ColumnDefault("true")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}