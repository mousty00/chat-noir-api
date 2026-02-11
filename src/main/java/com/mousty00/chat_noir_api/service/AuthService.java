package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.LoginRequest;
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
    public LoginResponse login(LoginRequest request) {
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
        
        return LoginResponse.builder()
            .token(token)
            .username(user.getUsername())
            .email(user.getEmail())
            .isAdmin(user.isAdmin())
            .roles(List.copyOf(roles))
            .build();
    }
    
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        UserRole defaultRole = userRoleRepository.findByName("USER")
            .orElseThrow(() -> new RuntimeException("Default role not found"));
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAdmin(false);
        user.setRole(defaultRole);
        
        userRepository.save(user);
    }
    
    @Transactional
    public void promoteToAdmin(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAdmin(true);
        userRepository.save(user);
    }
}