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

    @Value("${app.base-url:http://localhost:8080/api}")
    private String backendBaseUrl;

    @Value("${app.password-reset.expiry-minutes:60}")
    private long expiryMinutes;

    @Value("${app.email-verification.expiry-minutes:1440}")
    private long verificationExpiryMinutes;

    public boolean sendWelcomeEmail(String to, String username) {
        Context ctx = new Context();
        ctx.setVariable("username", username);
        ctx.setVariable("homeUrl", buildFrontendBaseUrl());

        String html = templateEngine.process("email/welcome", ctx);
        return sendEmail(to, "Welcome to Chat Noir", html);
    }

    public boolean sendVerificationEmail(String to, String username, String token) {
        Context ctx = new Context();
        ctx.setVariable("username", username);
        ctx.setVariable("verificationLink", backendBaseUrl + "/auth/verify-email?token=" + token);
        ctx.setVariable("expiryMinutes", verificationExpiryMinutes);

        String html = templateEngine.process("email/verify-email", ctx);
        return sendEmail(to, "Verify your Chat Noir account", html);
    }

    public boolean sendPasswordResetEmail(String to, String token) {
        Context ctx = new Context();
        ctx.setVariable("resetLink", buildFrontendBaseUrl() + "/reset-password?token=" + token);
        ctx.setVariable("expiryMinutes", expiryMinutes);

        String html = templateEngine.process("email/password-reset", ctx);
        return sendEmail(to, "Reset your Chat Noir password", html);
    }

    private boolean sendEmail(String to, String subject, String html) {
        if (!mailEnabled) {
            log.info("Skipping email delivery because mail delivery is disabled");
            return false;
        }

        if (from == null || from.isBlank()) {
            log.warn("Skipping email delivery because app.mail.from is not configured");
            return false;
        }

        if (!"resend".equalsIgnoreCase(provider)) {
            log.warn("Skipping email delivery because mail provider '{}' is not supported", provider);
            return false;
        }

        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("Skipping email delivery because app.mail.resend.api-key is not configured");
            return false;
        }

        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(resendBaseUrl)
                    .defaultHeader("Authorization", "Bearer " + resendApiKey)
                    .defaultHeader("User-Agent", "chat-noir-api/1.0")
                    .build();

            Map<String, Object> payload = Map.of(
                    "from", "Chat Noir <" + from + ">",
                    "to", List.of(to),
                    "subject", subject,
                    "html", html
            );

            ResendSendEmailResponse response = restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(ResendSendEmailResponse.class);

            log.info("Email sent to {} via Resend with id {}", to, response != null ? response.id() : "unknown");
            return true;
        } catch (RestClientException e) {
            log.warn("Email could not be delivered to {} via Resend: {}", to, e.getMessage());
            return false;
        }
    }

    private String buildFrontendBaseUrl() {
        boolean hasPort = feDomain.contains(":");
        return hasPort ? "http://" + feDomain : "https://" + feDomain;
    }

    private record ResendSendEmailResponse(String id) {
    }
}
