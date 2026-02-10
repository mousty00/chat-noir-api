package com.mousty00.chat_noir_api.repository;

import com.mousty00.chat_noir_api.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UUID> {
}
