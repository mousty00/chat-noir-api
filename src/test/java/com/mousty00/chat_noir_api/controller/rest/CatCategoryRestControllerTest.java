package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.service.CatCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatCategoryRestController")
class CatCategoryRestControllerTest {

    @Mock CatCategoryService catCategoryService;
    @InjectMocks CatCategoryRestController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /cats/categories/ returns all categories")
    void getCategories_returnsList() throws Exception {
        List<CatCategoryDTO> categories = List.of(
                CatCategoryDTO.builder().id(UUID.randomUUID()).name("Cyberpunk").mediaTypeHint("image").build(),
                CatCategoryDTO.builder().id(UUID.randomUUID()).name("Mech").mediaTypeHint("video").build()
        );
        when(catCategoryService.getCategories()).thenReturn(ApiResponse.success("", categories));

        mockMvc.perform(get("/cats/categories/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("Cyberpunk"))
                .andExpect(jsonPath("$.data[1].name").value("Mech"));
    }

    @Test
    @DisplayName("GET /cats/categories/ returns empty list when none exist")
    void getCategories_empty_returnsEmptyList() throws Exception {
        when(catCategoryService.getCategories()).thenReturn(ApiResponse.success("", List.of()));

        mockMvc.perform(get("/cats/categories/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
