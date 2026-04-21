package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.friendship.FriendshipDTO;
import com.mousty00.chat_noir_api.dto.friendship.FriendshipRequestDTO;
import com.mousty00.chat_noir_api.dto.friendship.FriendshipResponseDTO;
import com.mousty00.chat_noir_api.dto.user.UserFavoriteDTO;
import com.mousty00.chat_noir_api.service.FriendshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FriendshipRestController {

    private final FriendshipService service;

    @PostMapping
    public ApiResponse<FriendshipDTO> sendRequest(@RequestBody @Valid FriendshipRequestDTO request) {
        return service.sendRequest(request.addresseeId());
    }

    @PatchMapping("/{id}")
    public ApiResponse<FriendshipDTO> respond(
            @PathVariable UUID id,
            @RequestBody @Valid FriendshipResponseDTO response
    ) {
        return service.respond(id, response.accept());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> removeFriend(@PathVariable UUID id) {
        return service.removeFriend(id);
    }

    @GetMapping
    public ApiResponse<List<FriendshipDTO>> getFriends() {
        return service.getFriends();
    }

    @GetMapping("/requests")
    public ApiResponse<List<FriendshipDTO>> getPendingRequests() {
        return service.getPendingRequests();
    }

    @GetMapping("/users/{userId}/favorites")
    public ApiResponse<PaginatedResponse<UserFavoriteDTO>> getFriendFavorites(
            @PathVariable UUID userId,
            Integer page,
            Integer size
    ) {
        return service.getFriendFavorites(userId, page, size);
    }
}
