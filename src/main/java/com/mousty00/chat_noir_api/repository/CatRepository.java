package com.mousty00.chat_noir_api.repository;

import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatCategory;
import graphql.collect.ImmutableKit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CatRepository extends JpaRepository<Cat, UUID> {


    Optional<Cat> findById(UUID id);

    Page<Cat> findAllByCategory_Name(String categoryName, Pageable pageable);
}
