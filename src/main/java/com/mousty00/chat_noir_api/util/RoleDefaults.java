package com.mousty00.chat_noir_api.util;

import java.util.Arrays;
import java.util.List;

public enum RoleDefaults {
    ALLOWED_ROLES("ADMIN", "CREATOR", "DEVELOPER");

    public final List<String> allowedRoles;

    RoleDefaults(String... endpoints) {
        this.allowedRoles = Arrays.asList(endpoints);
    }
}