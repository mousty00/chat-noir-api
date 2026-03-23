package com.mousty00.chat_noir_api.config;

import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.service.AuthService;
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

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${frontend.domain:localhost:3000}")
    private String feDomain;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        if (oAuth2User == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth2 authentication failed");
            return;
        }

        LoginResponse loginResponse = authService.loginOrRegisterOAuth2User(oAuth2User);
        String token = loginResponse.token();

        boolean hasPort = feDomain.contains(":");
        String baseUrl = UriComponentsBuilder.newInstance()
                .scheme(hasPort ? "http" : "https")
                .host(hasPort ? feDomain.split(":")[0] : feDomain)
                .port(hasPort ? feDomain.split(":")[1] : null)
                .path("/oauth2/callback")
                .build()
                .toUriString();

        String redirectUrl = baseUrl + "#token=" + token;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}