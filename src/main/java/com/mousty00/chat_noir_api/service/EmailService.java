package com.mousty00.chat_noir_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final TemplateEngine templateEngine;
    private final RestClient.Builder restClientBuilder;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${app.mail.provider:resend}")
    private String provider;

    @Value("${app.mail.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.mail.resend.base-url:https://api.resend.com}")
    private String resendBaseUrl;

    @Value("${frontend.domain:localhost:3000}")
    private String feDomain;

    @Value("${app.password-reset.expiry-minutes:60}")
    private long expiryMinutes;

    public boolean sendPasswordResetEmail(String to, String token) {
        if (!mailEnabled) {
            log.info("Skipping password reset email because mail delivery is disabled");
            return false;
        }

        if (from == null || from.isBlank()) {
            log.warn("Skipping password reset email because app.mail.from is not configured");
            return false;
        }

        if (!"resend".equalsIgnoreCase(provider)) {
            log.warn("Skipping password reset email because mail provider '{}' is not supported", provider);
            return false;
        }

        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("Skipping password reset email because app.mail.resend.api-key is not configured");
            return false;
        }

        boolean hasPort = feDomain.contains(":");
        String baseUrl = hasPort ? "http://" + feDomain : "https://" + feDomain;
        String resetLink = baseUrl + "/reset-password?token=" + token;

        Context ctx = new Context();
        ctx.setVariable("resetLink", resetLink);
        ctx.setVariable("expiryMinutes", expiryMinutes);

        String html = templateEngine.process("email/password-reset", ctx);

        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(resendBaseUrl)
                    .defaultHeader("Authorization", "Bearer " + resendApiKey)
                    .defaultHeader("User-Agent", "chat-noir-api/1.0")
                    .build();

            Map<String, Object> payload = Map.of(
                    "from", "Chat Noir <" + from + ">",
                    "to", List.of(to),
                    "subject", "Reset your Chat Noir password",
                    "html", html
            );

            ResendSendEmailResponse response = restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(ResendSendEmailResponse.class);

            log.info("Password reset email sent to {} via Resend with id {}", to, response != null ? response.id() : "unknown");
            return true;
        } catch (RestClientException e) {
            log.warn("Password reset email could not be delivered to {} via Resend: {}", to, e.getMessage());
            return false;
        }
    }

    private record ResendSendEmailResponse(String id) {
    }
}
