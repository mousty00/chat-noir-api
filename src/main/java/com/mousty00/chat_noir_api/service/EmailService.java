package com.mousty00.chat_noir_api.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${frontend.domain:localhost:3000}")
    private String feDomain;

    @Value("${app.password-reset.expiry-minutes:60}")
    private long expiryMinutes;

    public boolean sendPasswordResetEmail(String to, String token) {
        boolean hasPort = feDomain.contains(":");
        String baseUrl = hasPort ? "http://" + feDomain : "https://" + feDomain;
        String resetLink = baseUrl + "/reset-password?token=" + token;

        Context ctx = new Context();
        ctx.setVariable("resetLink", resetLink);
        ctx.setVariable("expiryMinutes", expiryMinutes);

        String html = templateEngine.process("email/password-reset", ctx);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Chat Noir <" + from + ">");
            helper.setTo(to);
            helper.setSubject("Reset your Chat Noir password");
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
            return true;
        } catch (MessagingException | MailException e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }
}
