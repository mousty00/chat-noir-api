package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.user.UserDTO;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserRole;
import com.mousty00.chat_noir_api.exception.AuthenticationException;
import com.mousty00.chat_noir_api.exception.UserException;
import com.mousty00.chat_noir_api.mapper.UserMapper;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserService")
class UserServiceTest {

    @Mock UserMapper userMapper;
    @Mock UserRepository userRepository;
    @Mock MediaService mediaService;
    @Mock S3Service s3Service;

    @InjectMocks UserService service;

    UUID userId;
    User user;
    UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        UserRole role = UserRole.builder().id(UUID.randomUUID()).name("USER").build();

        user = User.builder()
                .id(userId).username("testuser")
                .email("test@example.com").password("pw")
                .isAdmin(false).createdAt(Instant.now()).role(role)
                .build();

        userDTO = mock(UserDTO.class);
        when(userDTO.id()).thenReturn(userId);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("getUsers")
    class GetUsers {

        @Test
        @DisplayName("returns paginated users")
        void getUsers_returnsPaginatedList() {
            when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(user)));
            when(userMapper.toDTO(user)).thenReturn(userDTO);

            ApiResponse<PaginatedResponse<UserDTO>> response = service.getUsers(0, 10, null);

            assertThat(response.success()).isTrue();
            assertThat(response.data().result()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("returns user when found")
        void getUserById_found() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userMapper.toDTO(user)).thenReturn(userDTO);

            ApiResponse<UserDTO> response = service.getUserById(userId);

            assertThat(response.success()).isTrue();
            assertThat(response.data().id()).isEqualTo(userId);
        }

        @Test
        @DisplayName("throws UserException when not found")
        void getUserById_notFound_throwsException() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getUserById(userId))
                    .isInstanceOf(UserException.class);
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("deletes user and returns success")
        void deleteUser_success() {
            doNothing().when(userRepository).deleteById(userId);

            ApiResponse<?> response = service.deleteUser(userId);

            assertThat(response.success()).isTrue();
            verify(userRepository).deleteById(userId);
        }

        @Test
        @DisplayName("throws UserException when delete fails")
        void deleteUser_repositoryThrows_throwsException() {
            doThrow(new RuntimeException("DB error")).when(userRepository).deleteById(userId);

            assertThatThrownBy(() -> service.deleteUser(userId))
                    .isInstanceOf(UserException.class);
        }
    }

    @Nested
    @DisplayName("uploadProfileImage")
    class UploadProfileImage {

        @BeforeEach
        void setAuth() {
            var auth = new UsernamePasswordAuthenticationToken("testuser", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @Test
        @DisplayName("uploads profile image and returns presigned URL")
        void upload_ownerUser_success() throws Exception {
            MockMultipartFile file = new MockMultipartFile("imageFile", "profile.jpg", "image/jpeg", new byte[]{1, 2, 3});

            doNothing().when(mediaService).validateImageFile(any());
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(mediaService.sanitizeExtension(any())).thenReturn("jpg");
            when(mediaService.generateUserImageKey(anyString(), any(), anyString())).thenReturn("users/testuser/profile.jpg");
            when(s3Service.uploadFileAsync(any(), anyString())).thenReturn(CompletableFuture.completedFuture("users/testuser/profile.jpg"));
            when(s3Service.generatePresignedUrl("users/testuser/profile.jpg")).thenReturn("https://s3/presigned");
            when(userRepository.save(any())).thenReturn(user);

            ApiResponse<String> response = service.uploadProfileImage(file, userId);

            assertThat(response.success()).isTrue();
            assertThat(response.data()).contains("presigned");
        }

        @Test
        @DisplayName("returns bad request when authenticated user is not the owner")
        void upload_differentUser_returnsForbidden() {
            MockMultipartFile file = new MockMultipartFile("imageFile", "profile.jpg", "image/jpeg", new byte[]{1});
            User otherUser = User.builder()
                    .id(userId).username("otheruser")
                    .email("other@example.com").password("pw")
                    .isAdmin(false).createdAt(Instant.now())
                    .role(user.getRole())
                    .build();

            doNothing().when(mediaService).validateImageFile(any());
            when(userRepository.findById(userId)).thenReturn(Optional.of(otherUser));

            ApiResponse<String> response = service.uploadProfileImage(file, userId);

            assertThat(response.success()).isFalse();
        }

        @Test
        @DisplayName("returns bad request on invalid file")
        void upload_invalidFile_returnsBadRequest() {
            MockMultipartFile file = new MockMultipartFile("imageFile", "doc.pdf", "application/pdf", new byte[]{});
            doThrow(new IllegalArgumentException("Invalid file type")).when(mediaService).validateImageFile(any());

            ApiResponse<String> response = service.uploadProfileImage(file, userId);

            assertThat(response.success()).isFalse();
        }
    }
}
