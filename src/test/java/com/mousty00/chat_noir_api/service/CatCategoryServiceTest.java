package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.entity.CatCategory;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.mapper.CatCategoryMapper;
import com.mousty00.chat_noir_api.repository.CatCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatCategoryService")
class CatCategoryServiceTest {

    @Mock CatCategoryRepository catCategoryRepository;
    @Mock CatCategoryMapper catCategoryMapper;

    @InjectMocks CatCategoryService service;

    @Test
    @DisplayName("returns all categories mapped to DTOs")
    void getCategories_returnsMappedDTOs() {
        CatCategory c1 = CatCategory.builder().id(UUID.randomUUID()).name("Cyberpunk").mediaTypeHint("image").build();
        CatCategory c2 = CatCategory.builder().id(UUID.randomUUID()).name("Mech").mediaTypeHint("video").build();
        CatCategoryDTO d1 = CatCategoryDTO.builder().id(c1.getId()).name("Cyberpunk").mediaTypeHint("image").build();
        CatCategoryDTO d2 = CatCategoryDTO.builder().id(c2.getId()).name("Mech").mediaTypeHint("video").build();

        when(catCategoryRepository.findAll()).thenReturn(List.of(c1, c2));
        when(catCategoryMapper.toDTO(c1)).thenReturn(d1);
        when(catCategoryMapper.toDTO(c2)).thenReturn(d2);

        ApiResponse<List<CatCategoryDTO>> response = service.getCategories();

        assertThat(response.success()).isTrue();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data()).extracting(CatCategoryDTO::name).containsExactly("Cyberpunk", "Mech");
    }

    @Test
    @DisplayName("returns empty list when no categories exist")
    void getCategories_empty_returnsEmptyList() {
        when(catCategoryRepository.findAll()).thenReturn(List.of());

        ApiResponse<List<CatCategoryDTO>> response = service.getCategories();

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEmpty();
    }

    @Test
    @DisplayName("throws CatException when repository throws")
    void getCategories_repositoryThrows_throwsCatException() {
        when(catCategoryRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> service.getCategories())
                .isInstanceOf(CatException.class);
    }
}
