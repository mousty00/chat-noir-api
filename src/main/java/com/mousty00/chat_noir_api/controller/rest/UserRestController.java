package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.user.UserDTO;
import com.mousty00.chat_noir_api.service.UserService;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService service;

    @GetMapping("/users")
    public ApiResponse<PaginatedResponse<UserDTO>> getUsers(
            Integer page,
            Integer size,
            String username
    ) {
        return service.getUsers(page, size, username);
    }

    @GetMapping("/users/{id}")
    public ApiResponse<UserDTO> getUserById(@PathVariable UUID id) {
        return service.getUserById(id);
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<?> deleteUser(@PathVariable UUID id) { return service.deleteUser(id); }

    @PostMapping(value = "/users/{id}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadProfileImage(@PathVariable UUID id, @RequestParam MultipartFile imageFile) {
        return service.uploadProfileImage(imageFile, id);
    }

}
