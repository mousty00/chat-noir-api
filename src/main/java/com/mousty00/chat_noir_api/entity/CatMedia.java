package com.mousty00.chat_noir_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cat_media")
public class CatMedia {
    @Id
    @GeneratedValue(generator = "UUID")
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "cat_id", nullable = false)
    private UUID catId;

    @Column(name = "media_format", nullable = false, length = 20)
    private String mediaFormat;

    @Column(name = "content_url", nullable = false, length = 512)
    private String contentUrl;

}