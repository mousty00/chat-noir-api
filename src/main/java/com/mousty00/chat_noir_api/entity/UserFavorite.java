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
@Table(name = "user_favorite")
@NoArgsConstructor
@AllArgsConstructor
public class UserFavorite {
    @Id
    @GeneratedValue(generator = "UUID")
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "cat_media_id", nullable = false)
    private UUID catMediaId;

}