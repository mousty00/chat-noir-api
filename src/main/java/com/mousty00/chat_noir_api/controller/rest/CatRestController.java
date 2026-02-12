package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatFilterDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.service.CatMediaService;
import com.mousty00.chat_noir_api.service.CatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class CatRestController {

    private final CatService service;
    private final CatMediaService mediaService;

    @GetMapping("/cats")
    public ApiResponse<PaginatedResponse<CatDTO>> getCats(Integer page, Integer size, CatFilterDTO filter) {
        return service.getCats(page, size, filter);
    }

    @GetMapping("/cats/{id}")
    public ApiResponse<CatDTO> getCat(@PathVariable UUID id) {
        return service.getCatById(id);
    }

    @DeleteMapping("/cats/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteCat(@PathVariable UUID id) {
        return service.deleteCat(id);
    }

    @PostMapping("/cats")
    @PreAuthorize("@roleChecker.hasAllowedRole(authentication)")
    public ApiResponse<CatDTO> createCat(@RequestBody @Valid CatRequestDTO request) {
        return service.saveCat(request);
    }

    @PutMapping("/cats/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CatDTO> updateCat(@PathVariable UUID id, @RequestBody @Valid CatDTO request) {
        return service.updateCat(id, request);
    }

    @PostMapping(value = "/cats/{id}/media",  consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadMedia(@PathVariable UUID id, @RequestParam  MultipartFile mediaFile) {
        return mediaService.uploadMedia(id, mediaFile);
    }
}
