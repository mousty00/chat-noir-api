package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatMediaStreamInfo;
import com.mousty00.chat_noir_api.service.CatMediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatMediaRestController")
class CatMediaRestControllerTest {

    @Mock CatMediaService catMediaService;
    @InjectMocks CatMediaRestController controller;

    MockMvc mockMvc;
    UUID catId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        catId = UUID.randomUUID();
    }

    @Test
    @DisplayName("POST /cats/{id}/media uploads media and returns presigned URL")
    void uploadMedia_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("mediaFile", "cat.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3});
        when(catMediaService.uploadMediaWithCleanup(any(), any()))
                .thenReturn(ApiResponse.success(200, "Media uploaded successfully", "https://s3/presigned"));

        mockMvc.perform(multipart("/cats/{id}/media", catId).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("https://s3/presigned"));
    }

    @Test
    @DisplayName("DELETE /cats/{id}/media deletes media and returns success")
    void deleteMedia_success() throws Exception {
        when(catMediaService.deleteCatMedia(catId))
                .thenReturn(ApiResponse.success("cat media deleted successfully!", null));

        mockMvc.perform(delete("/cats/{id}/media", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(catMediaService).deleteCatMedia(catId);
    }

    @Test
    @DisplayName("GET /cats/{id}/media/info returns stream metadata")
    void getMediaInfo_success() throws Exception {
        CatMediaStreamInfo streamInfo = CatMediaStreamInfo.builder()
                .streamUrl("http://localhost:8080/api/cats/" + catId + "/media/stream")
                .filename("shadow.jpg").contentType("image/jpeg")
                .contentLength(12345L).extension("jpg")
                .viewable(true).expiresInMinutes(15L)
                .build();
        when(catMediaService.getCatMediaStreamInfo(catId))
                .thenReturn(ApiResponse.success(200, "Media stream info retrieved successfully", streamInfo));

        mockMvc.perform(get("/cats/{id}/media/info", catId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.data.viewable").value(true));
    }
}
