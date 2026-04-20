package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.auth.LoginRequest;
import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.dto.auth.RegisterRequest;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserRole;
import com.mousty00.chat_noir_api.exception.AuthenticationException;
import com.mousty00.chat_noir_api.exception.UserException;
import com.mousty00.chat_noir_api.jwt.JwtUtil;
import com.mousty00.chat_noir_api.service.S3Service;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserRoleRepository userRoleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock S3Service s3Service;

    @InjectMocks AuthService service;

    UserRole defaultRole;
    User testUser;

    @BeforeEach
    void setUp() {
        defaultRole = UserRole.builder()
                .id(UUID.randomUUID())
                .name("USER")
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .isAdmin(false)
                .createdAt(Instant.now())
                .role(defaultRole)
                .build();
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("returns success response with token on valid credentials")
        void login_validCredentials_returnsToken() {
            LoginRequest request = new LoginRequest("testuser", "password123");
            when(userRepository.findByUsernameWithRole("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(jwtUtil.generateToken(eq("testuser"), eq("test@example.com"), any(), eq(false), any()))
                    .thenReturn("jwt-token");

            ApiResponse<LoginResponse> response = service.login(request);

            assertThat(response.success()).isTrue();
            assertThat(response.status()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.data()).isNotNull();
            assertThat(response.data().token()).isEqualTo("jwt-token");
            assertThat(response.data().username()).isEqualTo("testuser");
            assertThat(response.data().email()).isEqualTo("test@example.com");
            assertThat(response.data().isAdmin()).isFalse();
        }

        @Test
        @DisplayName("throws AuthenticationException when user not found")
        void login_userNotFound_throwsAuthenticationException() {
            LoginRequest request = new LoginRequest("unknown", "password");
            when(userRepository.findByUsernameWithRole("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.login(request))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid username or password");
        }

        @Test
        @DisplayName("throws AuthenticationException when user has no password (OAuth2 account)")
        void login_userHasNoPassword_throwsAuthenticationException() {
            testUser = User.builder()
                    .id(UUID.randomUUID()).username("oauthuser")
                    .email("oauth@example.com").password("")
                    .isAdmin(false).createdAt(Instant.now()).role(defaultRole)
                    .build();
            LoginRequest request = new LoginRequest("oauthuser", "anything");
            when(userRepository.findByUsernameWithRole("oauthuser")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> service.login(request))
                    .isInstanceOf(AuthenticationException.class);
        }

        @Test
        @DisplayName("throws AuthenticationException when password does not match")
        void login_wrongPassword_throwsAuthenticationException() {
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");
            when(userRepository.findByUsernameWithRole("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

            assertThatThrownBy(() -> service.login(request))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid username or password");
        }

        @Test
        @DisplayName("includes ADMIN role in token when user isAdmin=true")
        void login_adminUser_includesAdminRole() {
            User adminUser = User.builder()
                    .id(UUID.randomUUID()).username("admin")
                    .email("admin@example.com").password("encoded")
                    .isAdmin(true).createdAt(Instant.now()).role(defaultRole)
                    .build();
            LoginRequest request = new LoginRequest("admin", "password");
            when(userRepository.findByUsernameWithRole("admin")).thenReturn(Optional.of(adminUser));
            when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
            when(jwtUtil.generateToken(eq("admin"), eq("admin@example.com"), argThat(roles -> roles.contains("ADMIN")), eq(true), any()))
                    .thenReturn("admin-token");

            ApiResponse<LoginResponse> response = service.login(request);

            assertThat(response.data().isAdmin()).isTrue();
            assertThat(response.data().roles()).contains("ADMIN");
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("creates user and returns success response")
        void register_newUser_success() {
            RegisterRequest request = new RegisterRequest("newuser", "new@example.com", "securepass");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userRoleRepository.findByName("USER")).thenReturn(Optional.of(defaultRole));

            ApiResponse<String> response = service.register(request);

            assertThat(response.success()).isTrue();
            assertThat(response.status()).isEqualTo(HttpStatus.CREATED.value());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("returns conflict error when username already exists")
        void register_duplicateUsername_returnsConflictError() {
            RegisterRequest request = new RegisterRequest("testuser", "other@example.com", "pass");
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            ApiResponse<String> response = service.register(request);

            assertThat(response.success()).isFalse();
            assertThat(response.status()).isEqualTo(HttpStatus.CONFLICT.value());
            assertThat(response.message()).containsIgnoringCase("username");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns conflict error when email already exists")
        void register_duplicateEmail_returnsConflictError() {
            RegisterRequest request = new RegisterRequest("newuser", "test@example.com", "pass");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            ApiResponse<String> response = service.register(request);

            assertThat(response.success()).isFalse();
            assertThat(response.status()).isEqualTo(HttpStatus.CONFLICT.value());
            assertThat(response.message()).containsIgnoringCase("email");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("encodes password before saving")
        void register_encodesPassword() {
            RegisterRequest request = new RegisterRequest("user", "u@example.com", "plaintext");
            when(userRepository.existsByUsername(any())).thenReturn(false);
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(userRoleRepository.findByName("USER")).thenReturn(Optional.of(defaultRole));
            when(passwordEncoder.encode("plaintext")).thenReturn("$hashed$");

            service.register(request);

            verify(passwordEncoder).encode("plaintext");
            verify(userRepository).save(argThat(u -> "$hashed$".equals(u.getPassword())));
        }
    }

    @Nested
    @DisplayName("loginOrRegisterOAuth2User")
    class OAuth2Login {

        @Test
        @DisplayName("logs in existing OAuth2 user by email")
        void oauth2_existingUser_returnsLoginResponse() {
            OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
            when(oAuth2User.getAttribute("name")).thenReturn("Test User");
            when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-123");
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtUtil.generateToken(any(), any(), any(), anyBoolean(), any())).thenReturn("oauth-token");

            LoginResponse response = service.loginOrRegisterOAuth2User(oAuth2User);

            assertThat(response.token()).isEqualTo("oauth-token");
            assertThat(response.username()).isEqualTo("testuser");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("registers new user when OAuth2 email not found")
        void oauth2_newUser_createsAndReturnsLoginResponse() {
            OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getAttribute("email")).thenReturn("new@gmail.com");
            when(oAuth2User.getAttribute("name")).thenReturn("New User");
            when(oAuth2User.getAttribute("sub")).thenReturn("google-sub-456");
            when(userRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());
            when(userRoleRepository.findByName("USER")).thenReturn(Optional.of(defaultRole));
            when(userRepository.existsByUsername(any())).thenReturn(false);
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(jwtUtil.generateToken(any(), any(), any(), anyBoolean(), any())).thenReturn("new-oauth-token");

            LoginResponse response = service.loginOrRegisterOAuth2User(oAuth2User);

            assertThat(response.token()).isEqualTo("new-oauth-token");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("throws AuthenticationException when email attribute is null")
        void oauth2_nullEmail_throwsAuthenticationException() {
            OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getAttribute("email")).thenReturn(null);

            assertThatThrownBy(() -> service.loginOrRegisterOAuth2User(oAuth2User))
                    .isInstanceOf(AuthenticationException.class);
        }
    }

    @Nested
    @DisplayName("promoteToAdmin")
    class PromoteToAdmin {

        @Test
        @DisplayName("sets isAdmin=true and saves user")
        void promoteToAdmin_success() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            ApiResponse<String> response = service.promoteToAdmin("testuser");

            assertThat(response.success()).isTrue();
            assertThat(testUser.isAdmin()).isTrue();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("throws UserException when user not found")
        void promoteToAdmin_userNotFound_throwsException() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.promoteToAdmin("ghost"))
                    .isInstanceOf(UserException.class);
        }
    }
}
