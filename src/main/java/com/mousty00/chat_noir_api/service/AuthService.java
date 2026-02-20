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

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw AuthenticationException.badCredentials();
        }

        Set<String> roles = new HashSet<>();
        roles.add(user.getRole().getName());
        if (user.isAdmin()) {
            roles.add("ADMIN");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                List.copyOf(roles),
                user.isAdmin()
        );

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .isAdmin(user.isAdmin())
                .roles(List.copyOf(roles))
                .build();

        return ApiResponse.success(HttpStatus.OK.value(), "Login successful", response);
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
    public ApiResponse<String> promoteToAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserException::userNotFound);

        user.setAdmin(true);
        userRepository.save(user);

        return ApiResponse.success("User promoted to admin successfully", username);
    }
}