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
@Table(name = "user_role")
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    @Id
    @GeneratedValue(generator = "UUID")
    @ColumnDefault("gen_random_uuid()")
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 25)
    private String name;

    @Column(name = "description", length = 100)
    private String description;

}