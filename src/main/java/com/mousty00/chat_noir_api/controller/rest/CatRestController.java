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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class CatRestController {

    private final CatService service;

    @GetMapping("/cats")
    public ApiResponse<PaginatedResponse<CatDTO>> getCats(Integer page, Integer size, CatFilterDTO filter) {
        return service.getCats(page, size, filter);
    }

    @GetMapping("/cats/{id}")
    public ApiResponse<CatDTO> getCat(@PathVariable("id") UUID catId) {
        return service.getCatById(catId);
    }

    @DeleteMapping("/cats/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteCat(@PathVariable("id") UUID catId) {
        return service.deleteCat(catId);
    }

    @PostMapping("/cats")
    @PreAuthorize("@roleChecker.hasAllowedRole(authentication)")
    public ApiResponse<CatDTO> createCat(@RequestBody @Valid CatRequestDTO request) {
        return service.saveCat(request);
    }

    @PutMapping("/cats/{id}")
    @PreAuthorize("@roleChecker.hasAllowedRole(authentication)")
    public ApiResponse<CatDTO> updateCat(@PathVariable("id") UUID catId, @RequestBody @Valid CatDTO request) {
        return service.updateCat(catId, request);
    }
}
