package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.auth.LoginRequest;
import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.dto.auth.RegisterRequest;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserRole;
import com.mousty00.chat_noir_api.exception.AuthenticationException;
import com.mousty00.chat_noir_api.exception.ResourceNotFoundException;
import com.mousty00.chat_noir_api.exception.UserException;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.repository.UserRoleRepository;
import com.mousty00.chat_noir_api.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        User user = userRepository.findByUsernameWithRole(request.getUsername())
                .orElseThrow(AuthenticationException::badCredentials);

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw AuthenticationException.badCredentials();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw AuthenticationException.badCredentials();
        }

        return ApiResponse.success(HttpStatus.OK.value(), "Login successful", buildLoginResponse(user));
    }

    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.error(HttpStatus.CONFLICT.value(), "Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error(HttpStatus.CONFLICT.value(), "Email already exists");
        }

        UserRole defaultRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found", "ROLE_001"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAdmin(false)
                .createdAt(Instant.now())
                .role(defaultRole)
                .build();

        userRepository.save(user);
        return ApiResponse.success(HttpStatus.CREATED.value(), "User registered successfully", "");
    }

    @Transactional
    public LoginResponse loginOrRegisterOAuth2User(OAuth2User oAuth2User) {
        String email    = oAuth2User.getAttribute("email");
        String name     = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");

        if (email == null) {
            throw AuthenticationException.badCredentials();
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerOAuth2User(email, name, googleId));

        return buildLoginResponse(user);
    }

    private User registerOAuth2User(String email, String name, String googleId) {
        UserRole defaultRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found", "ROLE_001"));

        String baseUsername = name != null
                ? name.replaceAll("\\s+", "_").toLowerCase()
                : email.split("@")[0];

        String username = resolveUniqueUsername(baseUsername);

        User newUser = User.builder()
                .username(username)
                .email(email)
                .password("")
                .googleId(googleId)
                .isAdmin(false)
                .createdAt(Instant.now())
                .role(defaultRole)
                .build();

        return userRepository.save(newUser);
    }

    @Transactional
    public ApiResponse<String> promoteToAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserException::userNotFound);

        user.setAdmin(true);
        userRepository.save(user);
        return ApiResponse.success("User promoted to admin successfully", username);
    }

    private LoginResponse buildLoginResponse(User user) {
        Set<String> roles = new HashSet<>();
        roles.add(user.getRole().getName());
        if (user.isAdmin()) roles.add("ADMIN");

        String token = jwtUtil.generateToken(
                user.getUsername(),
                List.copyOf(roles),
                user.isAdmin()
        );

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .isAdmin(user.isAdmin())
                .roles(List.copyOf(roles))
                .build();
    }

    private String resolveUniqueUsername(String base) {
        if (!userRepository.existsByUsername(base)) return base;

        int suffix = 1;
        while (userRepository.existsByUsername(base + "_" + suffix)) {
            suffix++;
        }
        return base + "_" + suffix;
    }
}