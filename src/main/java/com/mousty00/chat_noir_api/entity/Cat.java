package com.mousty00.chat_noir_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cat")
public class Cat {
    @Id
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 25)
    private String name;

    @Column(name = "color", length = 25)
    private String color;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "source_name", length = 50)
    private String sourceName;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "cat_id", insertable = false, updatable = false)
    private CatMedia media;

}