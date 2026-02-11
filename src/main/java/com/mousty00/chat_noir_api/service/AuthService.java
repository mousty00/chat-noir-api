package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.LoginRequest;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.dto.auth.RegisterRequest;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserRole;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.repository.UserRoleRepository;
import com.mousty00.chat_noir_api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        try {
            User user = userRepository.findByUsernameWithRole(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
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

            return ApiResponse.<LoginResponse>builder()
                    .status(200)
                    .error(false)
                    .success(true)
                    .data(response)
                    .build();

        } catch (Exception e) {
            return ApiResponse.<LoginResponse>builder()
                    .status(401)
                    .error(true)
                    .success(false)
                    .message("Invalid credentials")
                    .build();
        }
    }
    
    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }

            UserRole defaultRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .isAdmin(false)
                    .role(defaultRole)
                    .build();

            userRepository.save(user);

            return ApiResponse.<String>builder()
                    .status(201)
                    .error(false)
                    .success(true)
                    .message("User registered successfully")
                    .data("")
                    .build();

        } catch (RuntimeException e) {
            return ApiResponse.<String>builder()
                    .status(400)
                    .error(true)
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }
    
    @Transactional
    public void promoteToAdmin(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAdmin(true);
        userRepository.save(user);
    }
}