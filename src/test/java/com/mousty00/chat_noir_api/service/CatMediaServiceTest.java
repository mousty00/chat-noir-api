package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatMediaStreamInfo;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatMedia;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserRole;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.exception.MediaException;
import com.mousty00.chat_noir_api.repository.CatMediaRepository;
import com.mousty00.chat_noir_api.repository.CatRepository;
import com.mousty00.chat_noir_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatMediaService")
class CatMediaServiceTest {

    @Mock S3Service s3Service;
    @Mock MediaService mediaService;
    @Mock CatMediaRepository catMediaRepository;
    @Mock CatRepository catRepository;
    @Mock UserRepository userRepository;

    @InjectMocks CatMediaService service;

    UUID catId;
    Cat cat;
    CatMedia catMedia;
    User adminUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");

        catId = UUID.randomUUID();
        UserRole role = UserRole.builder().id(UUID.randomUUID()).name("USER").build();

        cat = Cat.builder().id(catId).name("Shadow").build();

        catMedia = CatMedia.builder()
                .id(UUID.randomUUID())
                .cat(cat)
                .mediaKey("users/admin/cats/" + catId + "/image.jpg")
                .mediaFormat("image/jpeg")
                .build();
        cat.setMedia(catMedia);

        adminUser = User.builder()
                .id(UUID.randomUUID()).username("admin")
                .email("admin@example.com").password("pw")
                .isAdmin(true).createdAt(Instant.now()).role(role)
                .build();

        var auth = new UsernamePasswordAuthenticationToken("admin", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("uploadMediaWithCleanup")
    class UploadMedia {

        @Test
        @DisplayName("uploads image and returns presigned URL on success")
        void upload_adminUser_success() throws Exception {
            MockMultipartFile file = new MockMultipartFile("mediaFile", "cat.jpg", "image/jpeg", new byte[]{1, 2, 3});

            doNothing().when(mediaService).validateImageFile(any());
            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(mediaService.sanitizeExtension(any())).thenReturn("jpg");
            when(mediaService.generateCatImageKey(anyString(), any(), anyString())).thenReturn("cats/admin/shadow.jpg");
            when(s3Service.uploadFileAsync(any(), anyString())).thenReturn(CompletableFuture.completedFuture("cats/admin/shadow.jpg"));
            when(catMediaRepository.findByCatId(catId)).thenReturn(Optional.of(catMedia));
            when(catMediaRepository.save(any())).thenReturn(catMedia);
            when(catRepository.save(any())).thenReturn(cat);
            when(s3Service.generatePresignedUrl("cats/admin/shadow.jpg")).thenReturn("https://s3.amazonaws.com/presigned");

            ApiResponse<String> response = service.uploadMediaWithCleanup(catId, file);

            assertThat(response.success()).isTrue();
            assertThat(response.data()).contains("presigned");
        }

        @Test
        @DisplayName("throws MediaException when file validation fails")
        void upload_invalidFile_throwsMediaException() {
            MockMultipartFile file = new MockMultipartFile("mediaFile", "doc.pdf", "application/pdf", new byte[]{});
            doThrow(new IllegalArgumentException("Invalid file type")).when(mediaService).validateImageFile(any());

            assertThatThrownBy(() -> service.uploadMediaWithCleanup(catId, file))
                    .isInstanceOf(MediaException.class);
        }

        @Test
        @DisplayName("throws MediaException when cat not found")
        void upload_catNotFound_throwsMediaException() {
            MockMultipartFile file = new MockMultipartFile("mediaFile", "cat.jpg", "image/jpeg", new byte[]{1});
            doNothing().when(mediaService).validateImageFile(any());
            when(catRepository.findById(catId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.uploadMediaWithCleanup(catId, file))
                    .isInstanceOf(MediaException.class);
        }

        @Test
        @DisplayName("throws MediaException when non-admin user tries to upload")
        void upload_nonAdminUser_throwsMediaException() {
            MockMultipartFile file = new MockMultipartFile("mediaFile", "cat.jpg", "image/jpeg", new byte[]{1});
            User nonAdmin = User.builder()
                    .id(UUID.randomUUID()).username("admin")
                    .email("u@example.com").password("pw")
                    .isAdmin(false).createdAt(Instant.now())
                    .role(UserRole.builder().id(UUID.randomUUID()).name("USER").build())
                    .build();

            doNothing().when(mediaService).validateImageFile(any());
            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(nonAdmin));

            assertThatThrownBy(() -> service.uploadMediaWithCleanup(catId, file))
                    .isInstanceOf(MediaException.class);
        }
    }

    @Nested
    @DisplayName("getCatMediaStreamInfo")
    class GetStreamInfo {

        @Test
        @DisplayName("returns stream info for cat with media")
        void getStreamInfo_catHasMedia_returnsInfo() {
            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));
            when(s3Service.getFileContentType(catMedia.getMediaKey())).thenReturn("image/jpeg");
            when(s3Service.getFileSize(catMedia.getMediaKey())).thenReturn(12345L);

            ApiResponse<CatMediaStreamInfo> response = service.getCatMediaStreamInfo(catId);

            assertThat(response.success()).isTrue();
            assertThat(response.data().streamUrl()).contains(catId.toString());
            assertThat(response.data().contentType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("returns error when cat has no media")
        void getStreamInfo_noMedia_returnsError() {
            cat.setMedia(null);
            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));

            ApiResponse<CatMediaStreamInfo> response = service.getCatMediaStreamInfo(catId);

            assertThat(response.success()).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteCatMedia")
    class DeleteMedia {

        @Test
        @DisplayName("deletes media from S3 and repository")
        void deleteMedia_success() {
            when(catRepository.findById(catId)).thenReturn(Optional.of(cat));
            doNothing().when(s3Service).deleteFile(catMedia.getMediaKey());
            doNothing().when(catMediaRepository).delete(catMedia);

            ApiResponse<String> response = service.deleteCatMedia(catId);

            assertThat(response.success()).isTrue();
            verify(s3Service).deleteFile(catMedia.getMediaKey());
            verify(catMediaRepository).delete(catMedia);
        }

        @Test
        @DisplayName("throws CatException when cat not found")
        void deleteMedia_catNotFound_throwsCatException() {
            when(catRepository.findById(catId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteCatMedia(catId))
                    .isInstanceOf(CatException.class);
        }
    }
}
