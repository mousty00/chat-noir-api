package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.CatDTO;
import com.mousty00.chat_noir_api.pagination.PaginatedResponse;
import com.mousty00.chat_noir_api.response.ApiResponse;
import com.mousty00.chat_noir_api.service.CatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class CatRestController {

    private final CatService service;

    @GetMapping("/cats")
    public ApiResponse<PaginatedResponse<CatDTO>> getCats() {
        return service.getCats();
    }

    @GetMapping("/cats/{id}")
    public ApiResponse<CatDTO> getCat(@PathVariable UUID id) {
        return service.getCatById(id);
    }

    @DeleteMapping("/cats/{id}")
    public ApiResponse<?> deleteCat(@PathVariable UUID id) {
        return service.deleteCat(id);
    }

    @PostMapping("/cats")
    public ApiResponse<CatDTO> createCat(@RequestBody @Valid CatDTO request) {
        return service.saveCat(request);
    }

    @PutMapping("/cats/{id}")
    public ApiResponse<CatDTO> updateCat(@PathVariable UUID id, @RequestBody @Valid CatDTO request) {
        return service.updateCat(id, request);
    }
}
