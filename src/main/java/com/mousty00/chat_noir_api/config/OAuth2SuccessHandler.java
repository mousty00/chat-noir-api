package com.mousty00.chat_noir_api.config;

import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserRole;
import com.mousty00.chat_noir_api.exception.ResourceNotFoundException;
import com.mousty00.chat_noir_api.jwt.JwtUtil;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.repository.UserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtUtil jwtUtil;

    @Value("${frontend.domain}")
    private String FE_DOMAIN;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        assert oAuth2User != null;


        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            UserRole defaultRole = userRoleRepository.findByName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role not found", "ROLE_001"));

            return userRepository.save(User.builder()
                    .username(Objects.requireNonNull(name).replaceAll("\\s+", "_").toLowerCase())
                    .email(email)
                    .password("") // no password for oAuth users
                    .isAdmin(false)
                    .createdAt(Instant.now())
                    .role(defaultRole)
                    .build());
        });

        String token = jwtUtil.generateToken(
                user.getUsername(),
                List.of(user.getRole().getName()),
                user.isAdmin()
        );

        response.sendRedirect("http://%s/oauth2/callback?token=%s".formatted(FE_DOMAIN, token));
    }
}