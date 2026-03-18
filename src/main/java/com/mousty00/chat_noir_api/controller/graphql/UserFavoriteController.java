package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.user.UserFavoriteDTO;
import com.mousty00.chat_noir_api.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserFavoriteController {

    private final UserFavoriteService service;

    @QueryMapping
    public ApiResponse<PaginatedResponse<UserFavoriteDTO>> myFavorites(
            @Argument Integer page,
            @Argument Integer size
    ) {
        return service.getUserFavorites(page, size);
    }

    @QueryMapping
    public ApiResponse<Boolean> isFavorite(@Argument UUID catMediaId) {
        return service.isFavorite(catMediaId);
    }

    @MutationMapping
    public ApiResponse<UserFavoriteDTO> addFavorite(@Argument UUID catMediaId) {
        return service.addFavorite(catMediaId);
    }

    @MutationMapping
    public ApiResponse<?> removeFavorite(@Argument UUID catMediaId) {
        return service.removeFavorite(catMediaId);
    }
}
