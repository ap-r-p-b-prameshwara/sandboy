package com.sandbox.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String sandboxToken;
    private String tokenType = "Bearer";
    private Long expiresIn;

    public LoginResponse(String token, String sandboxToken, long expiresIn) {
        this.token = token;
        this.sandboxToken = sandboxToken;
        this.expiresIn = expiresIn;
    }
}
