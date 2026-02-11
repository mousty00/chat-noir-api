package com.mousty00.chat_noir_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "user_media")
@NoArgsConstructor
@AllArgsConstructor
public class UserMedia {
    @Id
    @GeneratedValue(generator = "UUID")
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "media_format", nullable = false, length = 50)
    private String mediaFormat;

    @Column(name = "url", nullable = false, length = 512)
    private String url;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

}