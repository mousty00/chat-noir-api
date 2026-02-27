package com.mousty00.chat_noir_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@Entity
@Table(name = "cat_media")
@NoArgsConstructor
@AllArgsConstructor
public class CatMedia {
    @Id
    @GeneratedValue(generator = "UUID")
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cat_id", nullable = false)
    @ToString.Exclude
    private Cat cat;

    @Column(name = "media_format", nullable = false, length = 20)
    private String mediaFormat;

    @Column(name = "media_key", nullable = false)
    private String mediaKey;

    @Column(name = "content_url", nullable = false, length = 512)
    private String contentUrl;
}