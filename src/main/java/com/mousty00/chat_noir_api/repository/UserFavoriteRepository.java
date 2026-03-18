package com.mousty00.chat_noir_api.repository;

import com.mousty00.chat_noir_api.entity.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UUID> {
    Page<UserFavorite> findByUserId(UUID userId, Pageable pageable);
    Optional<UserFavorite> findByUserIdAndCatMediaId(UUID userId, UUID catMediaId);
    boolean existsByUserIdAndCatMediaId(UUID userId, UUID catMediaId);
    void deleteByUserIdAndCatMediaId(UUID userId, UUID catMediaId);
}
