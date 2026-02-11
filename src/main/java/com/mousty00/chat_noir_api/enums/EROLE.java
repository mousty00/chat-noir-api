package com.mousty00.chat_noir_api.enums;

import java.util.Arrays;
import java.util.List;

public enum EROLE {
    ALLOWED_ROLES("ADMIN", "CREATOR", "DEVELOPER");

    public final List<String> allowedRoles;

    EROLE(String... endpoints) {
        this.allowedRoles = Arrays.asList(endpoints);
    }
}