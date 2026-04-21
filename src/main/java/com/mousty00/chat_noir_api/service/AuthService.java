package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.auth.ForgotPasswordRequest;
import com.mousty00.chat_noir_api.dto.auth.LoginRequest;
import com.mousty00.chat_noir_api.dto.auth.LoginResponse;
import com.mousty00.chat_noir_api.dto.auth.RegisterRequest;
import com.mousty00.chat_noir_api.dto.auth.ResetPasswordRequest;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserRole;
import com.mousty00.chat_noir_api.exception.AuthenticationException;
import com.mousty00.chat_noir_api.exception.ResourceNotFoundException;
import com.mousty00.chat_noir_api.exception.UserException;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.repository.UserRoleRepository;
import com.mousty00.chat_noir_api.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final S3Service s3Service;

    @Value("${app.password-reset.expiry-minutes:60}")
    private long passwordResetExpiryMinutes;

    @Value("${app.email-verification.expiry-minutes:1440}")
    private long emailVerificationExpiryMinutes;

    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        User user = userRepository.findByUsernameWithRole(request.username())
                .orElseThrow(AuthenticationException::badCredentials);

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw AuthenticationException.badCredentials();
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw AuthenticationException.badCredentials();
        }

        if (!user.isEmailVerified()) {
            throw AuthenticationException.emailNotVerified();
        }

        return ApiResponse.success(HttpStatus.OK.value(), "Login successful", buildLoginResponse(user));
    }

    @Transactional
    public ApiResponse<String> register(RegisterRequest request) {
        String normalizedEmail = request.email().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(request.username())) {
            return ApiResponse.error(HttpStatus.CONFLICT.value(), "Username already exists");
        }

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return ApiResponse.error(HttpStatus.CONFLICT.value(), "Email already exists");
        }

        UserRole defaultRole = userRoleRepository.findByName("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found", "ROLE_001"));

        User user = User.builder()
                .username(request.username())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .isAdmin(false)
                .emailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .emailVerificationTokenExpiry(Instant.now().plusSeconds(emailVerificationExpiryMinutes * 60))
                .createdAt(Instant.now())
                .role(defaultRole)
                .build();

        userRepository.save(user);
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), user.getEmailVerificationToken());
        return ApiResponse.success(HttpStatus.CREATED.value(), "User registered successfully. Please verify your email.", "");
    }

    @Transactional
    public LoginResponse loginOrRegisterOAuth2User(OAuth2User oAuth2User) {
        String rawEmail = oAuth2User.getAttribute("email");
        String name     = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");

        if (rawEmail == null) {
            throw AuthenticationException.badCredentials();
        }

        String email = rawEmail.toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailWithRole(email)
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
                .emailVerified(true)
                .createdAt(Instant.now())
                .role(defaultRole)
                .build();

        return userRepository.save(newUser);
    }

    @Transactional
    public ApiResponse<String> forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.email().toLowerCase(Locale.ROOT);
        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            Instant expiry = Instant.now().plusSeconds(passwordResetExpiryMinutes * 60);

            if (!emailService.sendPasswordResetEmail(user.getEmail(), token)) {
                return;
            }

            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(expiry);
            userRepository.save(user);
        });
        return ApiResponse.success(HttpStatus.OK.value(),
                "If an account with that email exists, a reset link has been sent.", "");
    }

    @Transactional
    public ApiResponse<String> verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired verification token", "AUTH_006", HttpStatus.BAD_REQUEST));

        if (user.getEmailVerificationTokenExpiry() == null || Instant.now().isAfter(user.getEmailVerificationTokenExpiry())) {
            throw new AuthenticationException("Verification token has expired", "AUTH_007", HttpStatus.BAD_REQUEST);
        }

        boolean firstVerification = !user.isEmailVerified();
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);

        if (firstVerification) {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        }

        return ApiResponse.success(HttpStatus.OK.value(), "Email verified successfully", "");
    }

    @Transactional
    public ApiResponse<String> resendVerificationEmail(ForgotPasswordRequest request) {
        String normalizedEmail = request.email().toLowerCase(Locale.ROOT);
        userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
            if (user.isEmailVerified() || user.getGoogleId() != null) {
                return;
            }

            String token = UUID.randomUUID().toString();
            Instant expiry = Instant.now().plusSeconds(emailVerificationExpiryMinutes * 60);

            if (!emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token)) {
                return;
            }

            user.setEmailVerificationToken(token);
            user.setEmailVerificationTokenExpiry(expiry);
            userRepository.save(user);
        });

        return ApiResponse.success(HttpStatus.OK.value(),
                "If an account with that email exists and is not verified, a verification link has been sent.", "");
    }

    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() -> new AuthenticationException("Invalid or expired reset token", "AUTH_003", HttpStatus.BAD_REQUEST));

        if (user.getPasswordResetTokenExpiry() == null || Instant.now().isAfter(user.getPasswordResetTokenExpiry())) {
            throw new AuthenticationException("Reset token has expired", "AUTH_004", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        return ApiResponse.success(HttpStatus.OK.value(), "Password reset successfully", "");
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
                user.getEmail(),
                List.copyOf(roles),
                user.isAdmin()
        );

        String image = user.getImageKey() != null ? s3Service.generatePresignedUrl(user.getImageKey()) : null;

        return LoginResponse.builder()
                .id(user.getId())
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .image(image)
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
