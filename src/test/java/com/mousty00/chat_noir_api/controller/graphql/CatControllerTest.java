package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatFilterDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.service.CatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatController (GraphQL)")
class CatControllerTest {

    @Mock CatService service;
    @InjectMocks CatController controller;

    UUID catId;
    CatDTO catDTO;
    CatCategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        catId = UUID.randomUUID();
        categoryDTO = CatCategoryDTO.builder().id(UUID.randomUUID()).name("Cyberpunk").mediaTypeHint("image").build();
        catDTO = CatDTO.builder().id(catId).name("Shadow").color("Black").category(categoryDTO).build();
    }

    @Nested
    @DisplayName("cats query")
    class CatsQuery {

        @Test
        @DisplayName("delegates to service with filter and returns paginated response")
        void cats_returnsPaginatedResponse() {
            PaginatedResponse<CatDTO> paginated = PaginatedResponse.<CatDTO>builder()
                    .result(List.of(catDTO)).currentPage(0).totalPages(1)
                    .totalItems(1).pageSize(12).hasNext(false).hasPrevious(false).build();
            when(service.getCats(any(), any(), any(CatFilterDTO.class)))
                    .thenReturn(ApiResponse.success(200, "OK", paginated));

            ApiResponse<PaginatedResponse<CatDTO>> result = controller.cats(0, 12, null, null, null, null);

            assertThat(result.success()).isTrue();
            assertThat(result.data().result()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("catById query")
    class CatByIdQuery {

        @Test
        @DisplayName("returns cat for given id")
        void catById_found_returnsCat() {
            when(service.getCatById(catId)).thenReturn(ApiResponse.success(200, "OK", catDTO));

            ApiResponse<CatDTO> result = controller.catById(catId);

            assertThat(result.success()).isTrue();
            assertThat(result.data().id()).isEqualTo(catId);
            assertThat(result.data().name()).isEqualTo("Shadow");
        }
    }

    @Nested
    @DisplayName("createCat mutation")
    class CreateCatMutation {

        @Test
        @DisplayName("delegates to service and returns created cat")
        void createCat_success() {
            CatRequestDTO request = new CatRequestDTO("Shadow", "Black", categoryDTO, null, "Neural Link");
            when(service.saveCat(request)).thenReturn(ApiResponse.success(200, "OK", catDTO));

            ApiResponse<CatDTO> result = controller.createCat(request);

            assertThat(result.success()).isTrue();
            assertThat(result.data().name()).isEqualTo("Shadow");
            verify(service).saveCat(request);
        }
    }

    @Nested
    @DisplayName("updateCat mutation")
    class UpdateCatMutation {

        @Test
        @DisplayName("delegates to service with id and request")
        void updateCat_success() {
            CatRequestDTO request = new CatRequestDTO("Shadow V2", "White", categoryDTO, null, null);
            CatDTO updated = CatDTO.builder().id(catId).name("Shadow V2").color("White").category(categoryDTO).build();
            when(service.updateCat(eq(catId), eq(request))).thenReturn(ApiResponse.success(200, "OK", updated));

            ApiResponse<CatDTO> result = controller.updateCat(catId, request);

            assertThat(result.success()).isTrue();
            assertThat(result.data().name()).isEqualTo("Shadow V2");
        }
    }

    @Nested
    @DisplayName("deleteCat mutation")
    class DeleteCatMutation {

        @Test
        @DisplayName("delegates to service and returns success")
        void deleteCat_success() {
            when(service.deleteCat(catId)).thenReturn(ApiResponse.success(200, "deleted", null));

            ApiResponse<?> result = controller.deleteCat(catId);

            assertThat(result.success()).isTrue();
            verify(service).deleteCat(catId);
        }
    }
}
