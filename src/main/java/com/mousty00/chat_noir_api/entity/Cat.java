package com.mousty00.chat_noir_api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Remove;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "cat")
@RequiredArgsConstructor
@AllArgsConstructor
public class Cat {
    @Id
    @GeneratedValue(generator = "UUID")
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 25)
    private String name;

    @Column(name = "color", length = 25)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CatCategory category;

    @Column(name = "source_name", length = 50)
    private String sourceName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "cat_id", insertable = false, updatable = false)
    private CatMedia media;
}