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
@Table(name = "cat_category")
@NoArgsConstructor
@AllArgsConstructor
public class CatCategory {
    @Id
    @GeneratedValue(generator = "UUID")
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "media_type_hint", nullable = false, length = 10)
    private String mediaTypeHint;

}