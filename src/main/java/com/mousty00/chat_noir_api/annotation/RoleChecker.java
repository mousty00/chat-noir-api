package com.mousty00.chat_noir_api.annotation;

import com.mousty00.chat_noir_api.util.RoleDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("roleChecker")
public class RoleChecker {
    public boolean hasAllowedRole( Authentication authentication) {
        return RoleDefaults.ALLOWED_ROLES.allowedRoles.stream()
            .anyMatch(role -> authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.requireNonNull(a.getAuthority()).equals("ROLE_" + role)));
    }
}