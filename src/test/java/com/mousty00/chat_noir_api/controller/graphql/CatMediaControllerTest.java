package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatMediaStreamInfo;
import com.mousty00.chat_noir_api.service.CatMediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatMediaController (GraphQL)")
class CatMediaControllerTest {

    @Mock CatMediaService service;
    @InjectMocks CatMediaController controller;

    UUID catId;

    @BeforeEach
    void setUp() {
        catId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("catMediaDownloadInfo query")
    class DownloadInfo {

        @Test
        @DisplayName("returns stream info for given cat id")
        void catMediaDownloadInfo_success() {
            CatMediaStreamInfo streamInfo = CatMediaStreamInfo.builder()
                    .streamUrl("http://localhost:8080/api/cats/" + catId + "/media/stream")
                    .filename("shadow.jpg").contentType("image/jpeg")
                    .contentLength(12345L).extension("jpg")
                    .viewable(true).expiresInMinutes(15L)
                    .build();
            when(service.getCatMediaStreamInfo(catId))
                    .thenReturn(ApiResponse.success(200, "OK", streamInfo));

            ApiResponse<CatMediaStreamInfo> result = controller.catMediaDownloadInfo(catId);

            assertThat(result.success()).isTrue();
            assertThat(result.data().contentType()).isEqualTo("image/jpeg");
            assertThat(result.data().viewable()).isTrue();
        }

        @Test
        @DisplayName("returns error response when cat has no media")
        void catMediaDownloadInfo_noMedia_returnsError() {
            when(service.getCatMediaStreamInfo(catId))
                    .thenReturn(ApiResponse.error(404, "Media not found"));

            ApiResponse<CatMediaStreamInfo> result = controller.catMediaDownloadInfo(catId);

            assertThat(result.success()).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteCatMedia mutation")
    class DeleteMedia {

        @Test
        @DisplayName("delegates to service and returns success")
        void deleteCatMedia_success() {
            when(service.deleteCatMedia(catId)).thenReturn(ApiResponse.success("deleted", null));

            ApiResponse<String> result = controller.deleteCatMedia(catId);

            assertThat(result.success()).isTrue();
            verify(service).deleteCatMedia(catId);
        }
    }

    @Nested
    @DisplayName("uploadCatMedia mutation")
    class UploadMedia {

        @Test
        @DisplayName("delegates to service and returns presigned URL")
        void uploadCatMedia_success() {
            MockMultipartFile file = new MockMultipartFile("file", "cat.jpg", "image/jpeg", new byte[]{1, 2, 3});
            when(service.uploadMediaWithCleanup(catId, file))
                    .thenReturn(ApiResponse.success(200, "Media uploaded successfully", "https://s3/presigned"));

            ApiResponse<String> result = controller.uploadCatMedia(catId, file);

            assertThat(result.success()).isTrue();
            assertThat(result.data()).isEqualTo("https://s3/presigned");
            verify(service).uploadMediaWithCleanup(catId, file);
        }
    }
}
