package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatFilterDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatCategory;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.mapper.CatMapper;
import com.mousty00.chat_noir_api.repository.CatCategoryRepository;
import com.mousty00.chat_noir_api.repository.CatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatService")
class CatServiceTest {

    @Mock CatRepository catRepository;
    @Mock CatMapper catMapper;
    @Mock CatCategoryRepository catCategoryRepository;

    @InjectMocks CatService service;

    UUID catId;
    UUID categoryId;
    CatCategory category;
    Cat cat;
    CatDTO catDTO;
    CatCategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        catId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        category = CatCategory.builder()
                .id(categoryId).name("Cyberpunk").mediaTypeHint("image")
                .build();

        cat = Cat.builder()
                .id(catId).name("Shadow").color("Black")
                .category(category).sourceName("Neural Link")
                .build();

        categoryDTO = CatCategoryDTO.builder()
                .id(categoryId).name("Cyberpunk").mediaTypeHint("image")
                .build();

        catDTO = CatDTO.builder()
                .id(catId).name("Shadow").color("Black")
                .category(categoryDTO).sourceName("Neural Link")
                .build();
    }

    @Nested
    @DisplayName("getCats")
    class GetCats {

        @Test
        @DisplayName("returns paginated list of cats")
        void getCats_returnsPaginatedResponse() {
            var page = new PageImpl<>(List.of(cat));
            when(catRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(catMapper.toDTO(cat)).thenReturn(catDTO);

            ApiResponse<PaginatedResponse<CatDTO>> response = service.getCats(0, 12, new CatFilterDTO(null, null, null, null));

            assertThat(response.success()).isTrue();
            assertThat(response.data().result()).hasSize(1);
            assertThat(response.data().result().get(0).name()).isEqualTo("Shadow");
        }

        @Test
        @DisplayName("returns empty result when no cats match filter")
        void getCats_emptyResult() {
            when(catRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            ApiResponse<PaginatedResponse<CatDTO>> response = service.getCats(0, 12, new CatFilterDTO("unknown", null, null, null));

            assertThat(response.success()).isTrue();
            assertThat(response.data().result()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCatById")
    class GetCatById {

        @Test
        @DisplayName("returns cat when found")
        void getCatById_found_returnsResponse() {
            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));
            when(catMapper.toDTO(cat)).thenReturn(catDTO);

            ApiResponse<CatDTO> response = service.getCatById(catId);

            assertThat(response.success()).isTrue();
            assertThat(response.data().id()).isEqualTo(catId);
        }

        @Test
        @DisplayName("throws CatException when not found")
        void getCatById_notFound_throwsCatException() {
            when(catRepository.findById(catId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getCatById(catId))
                    .isInstanceOf(CatException.class);
        }
    }

    @Nested
    @DisplayName("saveCat")
    class SaveCat {

        @Test
        @DisplayName("saves cat and returns success response")
        void saveCat_valid_success() {
            CatRequestDTO request = new CatRequestDTO("Shadow", "Black", categoryDTO, null, "Neural Link");
            when(catCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(catMapper.toEntityFromRequest(request)).thenReturn(cat);
            when(catRepository.save(cat)).thenReturn(cat);
            when(catMapper.toDTO(cat)).thenReturn(catDTO);

            ApiResponse<CatDTO> response = service.saveCat(request);

            assertThat(response.success()).isTrue();
            assertThat(response.data().name()).isEqualTo("Shadow");
            verify(catRepository).save(cat);
        }

        @Test
        @DisplayName("throws CatException when category is null")
        void saveCat_nullCategory_throwsCatException() {
            CatRequestDTO request = new CatRequestDTO("Shadow", "Black", null, null, null);

            assertThatThrownBy(() -> service.saveCat(request))
                    .isInstanceOf(CatException.class);
        }

        @Test
        @DisplayName("throws CatException when category not found")
        void saveCat_categoryNotFound_throwsCatException() {
            CatRequestDTO request = new CatRequestDTO("Shadow", "Black", categoryDTO, null, null);
            when(catCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.saveCat(request))
                    .isInstanceOf(CatException.class);
        }
    }

    @Nested
    @DisplayName("updateCat")
    class UpdateCat {

        @Test
        @DisplayName("updates cat and returns success response")
        void updateCat_valid_success() {
            CatRequestDTO request = new CatRequestDTO("Shadow Updated", "White", categoryDTO, null, "New Source");
            CatDTO updatedDTO = CatDTO.builder().id(catId).name("Shadow Updated").color("White")
                    .category(categoryDTO).sourceName("New Source").build();

            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));
            when(catCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(catMapper.toEntityFromRequest(request)).thenReturn(cat);
            when(catRepository.save(cat)).thenReturn(cat);
            when(catMapper.toDTO(cat)).thenReturn(updatedDTO);

            ApiResponse<CatDTO> response = service.updateCat(catId, request);

            assertThat(response.success()).isTrue();
            assertThat(response.data().name()).isEqualTo("Shadow Updated");
        }

        @Test
        @DisplayName("throws CatException when cat not found")
        void updateCat_catNotFound_throwsCatException() {
            CatRequestDTO request = new CatRequestDTO("X", "X", categoryDTO, null, null);
            when(catRepository.findById(catId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateCat(catId, request))
                    .isInstanceOf(CatException.class);
        }

        @Test
        @DisplayName("sets id on entity before saving")
        void updateCat_setsIdOnEntity() {
            CatRequestDTO request = new CatRequestDTO("Shadow", "Black", categoryDTO, null, null);
            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));
            when(catCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
            when(catMapper.toEntityFromRequest(request)).thenReturn(cat);
            when(catRepository.save(any(Cat.class))).thenReturn(cat);
            when(catMapper.toDTO(cat)).thenReturn(catDTO);

            service.updateCat(catId, request);

            verify(catRepository).save(argThat(c -> catId.equals(c.getId())));
        }
    }

    @Nested
    @DisplayName("deleteCat")
    class DeleteCat {

        @Test
        @DisplayName("deletes cat and returns success response")
        void deleteCat_success() {
            doNothing().when(catRepository).deleteById(catId);

            ApiResponse<?> response = service.deleteCat(catId);

            assertThat(response.success()).isTrue();
            verify(catRepository).deleteById(catId);
        }

        @Test
        @DisplayName("throws CatException when deleteById throws")
        void deleteCat_repositoryThrows_throwsCatException() {
            doThrow(new RuntimeException("DB error")).when(catRepository).deleteById(catId);

            assertThatThrownBy(() -> service.deleteCat(catId))
                    .isInstanceOf(CatException.class);
        }
    }
}
