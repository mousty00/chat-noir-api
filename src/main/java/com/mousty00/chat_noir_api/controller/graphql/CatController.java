package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.pagination.PaginatedResponse;
import com.mousty00.chat_noir_api.service.CatService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CatController {

    private final CatService service;

    @QueryMapping
    public ApiResponse<PaginatedResponse<CatDTO>> allCats(
            @Argument int page, 
            @Argument int size) {
        return service.getCats(page, size);
    }

    @QueryMapping
    public ApiResponse<CatDTO> catById(@Argument UUID id) {
        return service.getCatById(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CatDTO> createCat(@Argument CatRequestDTO cat) {
        return service.saveCat(cat);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CatDTO> updateCat(@Argument UUID id, @Argument CatDTO cat) {
        return service.updateCat(id, cat);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteCat(@Argument UUID id) {
        return service.deleteCat(id);
    }
}