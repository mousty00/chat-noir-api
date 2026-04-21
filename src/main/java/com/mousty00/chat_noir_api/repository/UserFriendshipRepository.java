package com.mousty00.chat_noir_api.repository;

import com.mousty00.chat_noir_api.entity.UserFriendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFriendshipRepository extends JpaRepository<UserFriendship, UUID> {

    Optional<UserFriendship> findByRequesterIdAndAddresseeId(UUID requesterId, UUID addresseeId);

    boolean existsByRequesterIdAndAddresseeId(UUID requesterId, UUID addresseeId);

    @Query("""
            SELECT f FROM UserFriendship f
            WHERE (f.requesterId = :userId OR f.addresseeId = :userId)
              AND f.status = 'ACCEPTED'
            """)
    List<UserFriendship> findAcceptedFriendships(@Param("userId") UUID userId);

    @Query("""
            SELECT f FROM UserFriendship f
            WHERE f.addresseeId = :userId AND f.status = 'PENDING'
            """)
    List<UserFriendship> findPendingRequests(@Param("userId") UUID userId);

    @Query("""
            SELECT COUNT(f) > 0 FROM UserFriendship f
            WHERE ((f.requesterId = :userId AND f.addresseeId = :otherId)
               OR  (f.requesterId = :otherId AND f.addresseeId = :userId))
              AND f.status = 'ACCEPTED'
            """)
    boolean areFriends(@Param("userId") UUID userId, @Param("otherId") UUID otherId);

    @Query("""
            SELECT f FROM UserFriendship f
            WHERE (f.requesterId = :userId AND f.addresseeId = :otherId)
               OR (f.requesterId = :otherId AND f.addresseeId = :userId)
            """)
    Optional<UserFriendship> findBetween(@Param("userId") UUID userId, @Param("otherId") UUID otherId);
}
