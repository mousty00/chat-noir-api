package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.user.UserFavoriteDTO;
import com.mousty00.chat_noir_api.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
@PreAuthorize("isAuthenticated()")
public class UserFavoriteRestController {

    private final UserFavoriteService service;

    @GetMapping
    public ApiResponse<PaginatedResponse<UserFavoriteDTO>> getMyFavorites(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return service.getUserFavorites(page, size);
    }

    @PostMapping("/{catId}")
    public ApiResponse<UserFavoriteDTO> addFavorite(@PathVariable UUID catId) {
        return service.addFavorite(catId);
    }

    @DeleteMapping("/{catId}")
    public ApiResponse<?> removeFavorite(@PathVariable UUID catId) {
        return service.removeFavorite(catId);
    }

    @GetMapping("/{catId}/check")
    public ApiResponse<Boolean> isFavorite(@PathVariable UUID catId) {
        return service.isFavorite(catId);
    }
}
