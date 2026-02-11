package com.mousty00.chat_noir_api.enums;

import java.util.Arrays;
import java.util.List;

public enum EPAGE {
    DEFAULT_SIZE(10),
    ALLOWED_ENDPOINTS("/cats", "/users", "/subscription-plans");

    public final int size;
    public final List<String> allowedEndpoints;

    EPAGE(int size) {
        this.size = size;
        this.allowedEndpoints = null;
    }

    EPAGE(String... endpoints) {
        this.size = 0;
        this.allowedEndpoints = Arrays.asList(endpoints);
    }
}