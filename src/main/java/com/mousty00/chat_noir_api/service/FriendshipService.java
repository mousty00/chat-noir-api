package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.friendship.FriendshipDTO;
import com.mousty00.chat_noir_api.dto.user.UserFavoriteDTO;
import com.mousty00.chat_noir_api.dto.user.UserPublicDTO;
import com.mousty00.chat_noir_api.entity.FriendshipStatus;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserFriendship;
import com.mousty00.chat_noir_api.exception.FriendshipException;
import com.mousty00.chat_noir_api.exception.UserException;
import com.mousty00.chat_noir_api.repository.UserFriendshipRepository;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.util.PageDefaults;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final UserFriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserFavoriteService userFavoriteService;
    private final S3Service s3Service;

    @Transactional
    public ApiResponse<FriendshipDTO> sendRequest(UUID addresseeId) {
        UUID requesterId = resolveCurrentUserId();

        if (requesterId.equals(addresseeId)) {
            throw FriendshipException.selfRequest();
        }

        userRepository.findById(addresseeId)
                .orElseThrow(UserException::userNotFound);

        // check both directions
        if (friendshipRepository.existsByRequesterIdAndAddresseeId(requesterId, addresseeId)
                || friendshipRepository.existsByRequesterIdAndAddresseeId(addresseeId, requesterId)) {
            throw FriendshipException.alreadyExists();
        }

        UserFriendship saved = friendshipRepository.save(
                UserFriendship.builder()
                        .requesterId(requesterId)
                        .addresseeId(addresseeId)
                        .status(FriendshipStatus.PENDING)
                        .build()
        );

        User addressee = userRepository.findById(addresseeId).orElseThrow(UserException::userNotFound);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Friend request sent",
                toDTO(saved, addressee, requesterId)
        );
    }

    @Transactional
    public ApiResponse<FriendshipDTO> respond(UUID friendshipId, boolean accept) {
        UUID currentUserId = resolveCurrentUserId();

        UserFriendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> FriendshipException.notFound(friendshipId));

        if (!friendship.getAddresseeId().equals(currentUserId)) {
            throw FriendshipException.notAddresseeOrRequester();
        }

        if (!accept) {
            friendshipRepository.delete(friendship);
            return ApiResponse.success(HttpStatus.OK.value(), "Friend request declined", null);
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        UserFriendship updated = friendshipRepository.save(friendship);

        User requester = userRepository.findById(friendship.getRequesterId())
                .orElseThrow(UserException::userNotFound);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Friend request accepted",
                toDTO(updated, requester, currentUserId)
        );
    }

    @Transactional
    public ApiResponse<?> removeFriend(UUID friendshipId) {
        UUID currentUserId = resolveCurrentUserId();

        UserFriendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> FriendshipException.notFound(friendshipId));

        if (!friendship.getRequesterId().equals(currentUserId)
                && !friendship.getAddresseeId().equals(currentUserId)) {
            throw FriendshipException.notAddresseeOrRequester();
        }

        friendshipRepository.delete(friendship);
        return ApiResponse.success(HttpStatus.OK.value(), "Friend removed", null);
    }

    public ApiResponse<List<FriendshipDTO>> getFriends() {
        UUID currentUserId = resolveCurrentUserId();

        List<UserFriendship> friendships = friendshipRepository.findAcceptedFriendships(currentUserId);
        List<UUID> friendIds = friendships.stream()
                .map(f -> f.getRequesterId().equals(currentUserId) ? f.getAddresseeId() : f.getRequesterId())
                .toList();

        Map<UUID, User> usersById = userRepository.findAllById(friendIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<FriendshipDTO> dtos = friendships.stream()
                .map(f -> {
                    UUID friendId = f.getRequesterId().equals(currentUserId) ? f.getAddresseeId() : f.getRequesterId();
                    return toDTO(f, usersById.get(friendId), currentUserId);
                })
                .toList();

        return ApiResponse.success(HttpStatus.OK.value(), "Friends retrieved", dtos);
    }

    public ApiResponse<List<FriendshipDTO>> getPendingRequests() {
        UUID currentUserId = resolveCurrentUserId();

        List<UserFriendship> pending = friendshipRepository.findPendingRequests(currentUserId);
        List<UUID> requesterIds = pending.stream().map(UserFriendship::getRequesterId).toList();

        Map<UUID, User> usersById = userRepository.findAllById(requesterIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<FriendshipDTO> dtos = pending.stream()
                .map(f -> toDTO(f, usersById.get(f.getRequesterId()), currentUserId))
                .toList();

        return ApiResponse.success(HttpStatus.OK.value(), "Pending requests retrieved", dtos);
    }

    public ApiResponse<PaginatedResponse<UserFavoriteDTO>> getFriendFavorites(UUID friendId, Integer page, Integer size) {
        UUID currentUserId = resolveCurrentUserId();

        if (!friendshipRepository.areFriends(currentUserId, friendId)) {
            throw FriendshipException.notFriends(friendId);
        }

        Pageable pageable = PageDefaults.of(page, size);
        return userFavoriteService.getUserFavoritesForUser(friendId, pageable);
    }

    public ApiResponse<PaginatedResponse<UserPublicDTO>> exploreUsers(String username, Integer page, Integer size) {
        UUID currentUserId = resolveCurrentUserId();
        Pageable pageable = PageDefaults.of(page, size);

        var usersPage = userRepository.findAll(
                (root, query, cb) -> username != null && !username.isBlank()
                        ? cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%")
                        : cb.conjunction(),
                pageable
        );

        // build friendship status map for found users
        List<UserFriendship> myFriendships = friendshipRepository.findAcceptedFriendships(currentUserId);
        List<UserFriendship> myPending = friendshipRepository.findPendingRequests(currentUserId);

        Set<UUID> acceptedIds = myFriendships.stream()
                .map(f -> f.getRequesterId().equals(currentUserId) ? f.getAddresseeId() : f.getRequesterId())
                .collect(Collectors.toSet());

        Set<UUID> pendingIds = myPending.stream()
                .map(UserFriendship::getRequesterId)
                .collect(Collectors.toSet());

        List<UserPublicDTO> dtos = usersPage.getContent().stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(u -> {
                    FriendshipStatus status = null;
                    if (acceptedIds.contains(u.getId())) status = FriendshipStatus.ACCEPTED;
                    else if (pendingIds.contains(u.getId())) status = FriendshipStatus.PENDING;
                    return toPublicDTO(u, status);
                })
                .toList();

        PaginatedResponse<UserPublicDTO> paginated = new PaginatedResponse<>(
                dtos,
                usersPage.getNumber(),
                usersPage.getTotalPages(),
                usersPage.getTotalElements(),
                usersPage.getSize(),
                usersPage.hasNext(),
                usersPage.hasPrevious()
        );

        return ApiResponse.success(HttpStatus.OK.value(), "Users retrieved", paginated);
    }

    private FriendshipDTO toDTO(UserFriendship f, User friend, UUID currentUserId) {
        return FriendshipDTO.builder()
                .id(f.getId())
                .friend(toPublicDTO(friend, f.getStatus()))
                .status(f.getStatus())
                .iAmRequester(f.getRequesterId().equals(currentUserId))
                .createdAt(f.getCreatedAt())
                .build();
    }

    private UserPublicDTO toPublicDTO(User user, FriendshipStatus status) {
        String imageUrl = user.getImageKey() != null
                ? s3Service.generatePresignedUrl(user.getImageKey())
                : null;
        return UserPublicDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .image(imageUrl)
                .friendStatus(status)
                .build();
    }

    private UUID resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = Objects.requireNonNull(auth).getName();
        return resolveUserIdByUsername(username);
    }

    @Cacheable(value = "userIds", key = "#username")
    public UUID resolveUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(UserException::userNotFound)
                .getId();
    }
}
