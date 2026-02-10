package com.mousty00.chat_noir_api.pagination;

public enum EPAGE {
    DEFAULT_SIZE(10);

    public final int size;

    EPAGE(int size) {
        this.size = size;
    }
}