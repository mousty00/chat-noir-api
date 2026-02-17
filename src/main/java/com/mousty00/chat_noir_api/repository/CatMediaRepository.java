package com.mousty00.chat_noir_api.repository;

import com.mousty00.chat_noir_api.entity.CatMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CatMediaRepository extends JpaRepository<CatMedia, UUID> {
    Optional<CatMedia> findByCatId(UUID catId);
}
