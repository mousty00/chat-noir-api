package com.mousty00.chat_noir_api.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    @PostConstruct
    private void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret (JWT_SECRET) must be configured and non-empty");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("jwt.secret (JWT_SECRET) must be at least 32 characters");
        }
    }

    public String generateToken(String username, String email, List<String> roles, boolean isAdmin, java.util.UUID userId) {
        List<String> springRoles = roles.stream()
                .map(role -> {
                    if (role.startsWith("ROLE_")) {
                        return role;
                    } else {
                        return "ROLE_" + role.toUpperCase();
                    }
                })
                .collect(Collectors.toList());

        if (isAdmin && !springRoles.contains("ROLE_ADMIN")) {
            springRoles.add("ROLE_ADMIN");
        }

        return JWT.create()
                .withSubject(username)
                .withClaim("userId", userId != null ? userId.toString() : null)
                .withClaim("username", username)
                .withClaim("email", email)
                .withClaim("roles", springRoles)
                .withClaim("isAdmin", isAdmin)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .sign(Algorithm.HMAC256(secret));
    }

    public DecodedJWT validateToken(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
    }
}