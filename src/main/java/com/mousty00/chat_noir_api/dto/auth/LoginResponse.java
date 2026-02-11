package com.mousty00.chat_noir_api.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private boolean isAdmin;
    private List<String> roles;
}