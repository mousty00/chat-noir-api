package com.mousty00.chat_noir_api.repository;

import com.mousty00.chat_noir_api.entity.Cat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CatRepository extends JpaRepository<Cat, UUID>, JpaSpecificationExecutor<Cat> {

    Optional<Cat> findById(UUID id);

}
