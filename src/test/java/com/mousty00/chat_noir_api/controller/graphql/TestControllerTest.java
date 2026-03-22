package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestController (GraphQL)")
class TestControllerTest {

    @InjectMocks TestController controller;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("adminTest query")
    class AdminTest {

        @BeforeEach
        void setAdminContext() {
            var auth = new UsernamePasswordAuthenticationToken(
                    "admin", null,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @Test
        @DisplayName("returns admin info message for authenticated admin user")
        void adminTest_authenticatedAdmin_returnsInfoMessage() {
            ApiResponse<String> result = controller.adminTest();

            assertThat(result.success()).isTrue();
            assertThat(result.data()).contains("admin");
            assertThat(result.data()).contains("ROLE_ADMIN");
            assertThat(result.data()).contains("true");
        }
    }

    @Nested
    @DisplayName("whoami query")
    class Whoami {

        @Test
        @DisplayName("returns user info string for authenticated user")
        void whoami_authenticated_returnsUserInfo() {
            var auth = new UsernamePasswordAuthenticationToken(
                    "testuser", null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            String result = controller.whoami();

            assertThat(result).contains("testuser");
            assertThat(result).contains("ROLE_USER");
        }

        @Test
        @DisplayName("returns 'Not authenticated' when no authentication present")
        void whoami_noAuthentication_returnsNotAuthenticated() {
            SecurityContextHolder.clearContext();

            String result = controller.whoami();

            assertThat(result).isEqualTo("Not authenticated");
        }
    }
}
