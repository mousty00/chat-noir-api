package com.mousty00.chat_noir_api.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.service.CatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatRestController")
class CatRestControllerTest {

    @Mock CatService catService;
    @InjectMocks CatRestController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;
    UUID catId;
    CatDTO catDTO;
    CatCategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        catId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        categoryDTO = CatCategoryDTO.builder().id(categoryId).name("Cyberpunk").mediaTypeHint("image").build();
        catDTO = CatDTO.builder().id(catId).name("Shadow").color("Black").category(categoryDTO).sourceName("Neural Link").build();
    }

    @Test
    @DisplayName("GET /cats returns paginated list")
    void getCats_returnsPaginatedResponse() throws Exception {
        PaginatedResponse<CatDTO> paginated = PaginatedResponse.<CatDTO>builder()
                .result(List.of(catDTO)).currentPage(0).totalPages(1)
                .totalItems(1).pageSize(12).hasNext(false).hasPrevious(false)
                .build();
        when(catService.getCats(any(), any(), any())).thenReturn(ApiResponse.success(200, "OK", paginated));

        mockMvc.perform(get("/cats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.result[0].name").value("Shadow"));
    }

    @Test
    @DisplayName("GET /cats/{id} returns cat by id")
    void getCatById_found_returnsCat() throws Exception {
        when(catService.getCatById(catId)).thenReturn(ApiResponse.success(200, "OK", catDTO));

        mockMvc.perform(get("/cats/{id}", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(catId.toString()))
                .andExpect(jsonPath("$.data.name").value("Shadow"));
    }

    @Test
    @DisplayName("GET /cats/{id} returns error response when not found")
    void getCatById_notFound_returnsError() throws Exception {
        when(catService.getCatById(catId)).thenReturn(ApiResponse.error(404, "Cat not found"));

        mockMvc.perform(get("/cats/{id}", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /cats creates cat and returns created DTO")
    void createCat_valid_returnsCreatedCat() throws Exception {
        CatRequestDTO request = new CatRequestDTO("Shadow", "Black", categoryDTO, null, "Neural Link");
        when(catService.saveCat(any(CatRequestDTO.class))).thenReturn(ApiResponse.success(200, "OK", catDTO));

        mockMvc.perform(post("/cats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Shadow"));
    }

    @Test
    @DisplayName("PUT /cats/{id} updates cat and returns updated DTO")
    void updateCat_valid_returnsUpdatedCat() throws Exception {
        CatRequestDTO request = new CatRequestDTO("Shadow V2", "White", categoryDTO, null, "Source");
        CatDTO updatedDTO = CatDTO.builder().id(catId).name("Shadow V2").color("White").category(categoryDTO).build();
        when(catService.updateCat(eq(catId), any(CatRequestDTO.class))).thenReturn(ApiResponse.success(200, "OK", updatedDTO));

        mockMvc.perform(put("/cats/{id}", catId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Shadow V2"));
    }

    @Test
    @DisplayName("DELETE /cats/{id} deletes cat and returns success")
    void deleteCat_returnsSuccessResponse() throws Exception {
        when(catService.deleteCat(catId)).thenReturn(ApiResponse.success(200, "deleted", null));

        mockMvc.perform(delete("/cats/{id}", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(catService).deleteCat(catId);
    }
}
