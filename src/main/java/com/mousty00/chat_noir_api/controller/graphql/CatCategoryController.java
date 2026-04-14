package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.dto.cat.CreateCategoryRequestDTO;
import com.mousty00.chat_noir_api.service.CatCategoryService;
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
public class CatCategoryController {

    private final CatCategoryService service;

    @QueryMapping
    public ApiResponse<List<CatCategoryDTO>> categories() {
        return service.getCategories();
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CatCategoryDTO> createCategory(@Argument CreateCategoryRequestDTO category) {
        return service.createCategory(category);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CatCategoryDTO> updateCategory(@Argument UUID id, @Argument CreateCategoryRequestDTO category) {
        return service.updateCategory(id, category);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CatCategoryDTO> deleteCategory(@Argument UUID id) {
        return service.deleteCategory(id);
    }

}
