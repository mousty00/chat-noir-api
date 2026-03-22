package com.mousty00.chat_noir_api.repository;

import com.mousty00.chat_noir_api.entity.CatSubmission;
import com.mousty00.chat_noir_api.entity.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CatSubmissionRepository extends JpaRepository<CatSubmission, UUID> {

    Page<CatSubmission> findByUserId(UUID userId, Pageable pageable);

    Page<CatSubmission> findByStatus(SubmissionStatus status, Pageable pageable);

    @Query("SELECT COUNT(s) FROM CatSubmission s WHERE s.userId = :userId AND s.createdAt >= :since")
    long countByUserIdSince(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}
