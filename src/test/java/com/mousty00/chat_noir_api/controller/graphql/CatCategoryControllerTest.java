package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.service.CatCategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatCategoryController (GraphQL)")
class CatCategoryControllerTest {

    @Mock CatCategoryService service;
    @InjectMocks CatCategoryController controller;

    @Test
    @DisplayName("categories query delegates to service and returns all categories")
    void categories_returnsMappedList() {
        List<CatCategoryDTO> categories = List.of(
                CatCategoryDTO.builder().id(UUID.randomUUID()).name("Cyberpunk").mediaTypeHint("image").build(),
                CatCategoryDTO.builder().id(UUID.randomUUID()).name("Mech").mediaTypeHint("video").build()
        );
        when(service.getCategories()).thenReturn(ApiResponse.success("", categories));

        ApiResponse<List<CatCategoryDTO>> result = controller.categories();

        assertThat(result.success()).isTrue();
        assertThat(result.data()).hasSize(2);
        assertThat(result.data()).extracting(CatCategoryDTO::name).containsExactly("Cyberpunk", "Mech");
        verify(service).getCategories();
    }

    @Test
    @DisplayName("categories query returns empty list when none exist")
    void categories_empty_returnsEmptyList() {
        when(service.getCategories()).thenReturn(ApiResponse.success("", List.of()));

        ApiResponse<List<CatCategoryDTO>> result = controller.categories();

        assertThat(result.data()).isEmpty();
    }
}
