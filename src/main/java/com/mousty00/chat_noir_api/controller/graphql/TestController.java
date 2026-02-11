package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class TestController {
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> adminTest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        List<String> roles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        
        String message = String.format(
            "Hello Admin! User: %s, Roles: %s, Authenticated: %s",
            auth.getName(),
            roles,
            auth.isAuthenticated()
        );
        
        return ApiResponse.<String>builder()
            .status(200)
            .error(false)
            .success(true)
            .data(message)
            .build();
    }
    
    @QueryMapping
    public String whoami() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return "Not authenticated";
        }
        
        List<String> roles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        return String.format(
            "User: %s, Roles: %s, Authenticated: %s",
            auth.getName(),
            roles,
            auth.isAuthenticated()
        );
    }
}