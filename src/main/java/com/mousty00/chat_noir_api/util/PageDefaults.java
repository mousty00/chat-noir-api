package com.mousty00.chat_noir_api.util;

import org.springframework.data.domain.PageRequest;

public enum PageDefaults {
    INSTANCE;

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public static PageRequest of(Integer page, Integer size) {
        return PageRequest.of(
                validatePage(page),
                validateSize(size)
        );
    }

    public static PageRequest of(Integer page, Integer size, org.springframework.data.domain.Sort sort) {
        return PageRequest.of(
                validatePage(page),
                validateSize(size),
                sort
        );
    }

    private static int validatePage(Integer page) {
        return page != null && page >= 0 ? page : DEFAULT_PAGE;
    }

    private static int validateSize(Integer size) {
        if (size == null) return DEFAULT_SIZE;
        return Math.min(Math.max(size, 1), MAX_SIZE);
    }

    public static boolean isValidPageable(Integer page, Integer size) {
        return page == null || (page >= 0 && size != null && size > 0 && size <= MAX_SIZE);
    }
}