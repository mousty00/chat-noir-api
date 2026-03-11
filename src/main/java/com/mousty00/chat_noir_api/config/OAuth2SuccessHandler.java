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
import org.springframework.web.util.UriComponentsBuilder;

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

    @Value("${frontend.domain:localhost:3000}")
    private String FE_DOMAIN;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        assert oAuth2User != null;

        String googleId = oAuth2User.getAttribute("sub");
        String email    = oAuth2User.getAttribute("email");
        String name     = oAuth2User.getAttribute("name");

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email).orElseGet(() -> {
                    UserRole defaultRole = userRoleRepository.findByName("USER")
                            .orElseThrow(() -> new ResourceNotFoundException("Default role not found", "ROLE_001"));

                    return userRepository.save(User.builder()
                            .username(Objects.requireNonNull(name).replaceAll("\\s+", "_").toLowerCase())
                            .email(email)
                            .googleId(googleId)
                            .password("")
                            .isAdmin(false)
                            .createdAt(Instant.now())
                            .role(defaultRole)
                            .build());
                }));

        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                List.of(user.getRole().getName()),
                user.isAdmin()
        );

        String redirectUrl = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(FE_DOMAIN.contains(":") ? FE_DOMAIN.split(":")[0] : FE_DOMAIN)
                .port(FE_DOMAIN.contains(":") ? FE_DOMAIN.split(":")[1] : null)
                .path("/oauth2/callback")
                .queryParam("token", token)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}