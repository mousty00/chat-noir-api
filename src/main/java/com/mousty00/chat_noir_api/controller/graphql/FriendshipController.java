package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.friendship.FriendshipDTO;
import com.mousty00.chat_noir_api.dto.user.UserFavoriteDTO;
import com.mousty00.chat_noir_api.dto.user.UserPublicDTO;
import com.mousty00.chat_noir_api.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FriendshipController {

    private final FriendshipService service;

    @QueryMapping
    public ApiResponse<List<FriendshipDTO>> myFriends() {
        return service.getFriends();
    }

    @QueryMapping
    public ApiResponse<List<FriendshipDTO>> pendingRequests() {
        return service.getPendingRequests();
    }

    @QueryMapping
    public ApiResponse<PaginatedResponse<UserPublicDTO>> exploreUsers(
            @Argument String username,
            @Argument Integer page,
            @Argument Integer size
    ) {
        return service.exploreUsers(username, page, size);
    }

    @QueryMapping
    public ApiResponse<PaginatedResponse<UserFavoriteDTO>> friendFavorites(
            @Argument UUID friendId,
            @Argument Integer page,
            @Argument Integer size
    ) {
        return service.getFriendFavorites(friendId, page, size);
    }

    @MutationMapping
    public ApiResponse<FriendshipDTO> sendFriendRequest(@Argument UUID addresseeId) {
        return service.sendRequest(addresseeId);
    }

    @MutationMapping
    public ApiResponse<FriendshipDTO> respondFriendRequest(
            @Argument UUID friendshipId,
            @Argument boolean accept
    ) {
        return service.respond(friendshipId, accept);
    }

    @MutationMapping
    public ApiResponse<?> removeFriend(@Argument UUID friendshipId) {
        return service.removeFriend(friendshipId);
    }
}
