package com.sandbox.authservice.controller;

import com.sandbox.authservice.dto.LoginRequest;
import com.sandbox.authservice.dto.LoginResponse;
import com.sandbox.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        
        if (!authService.verifyToken(token)) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("userId", authService.getUserIdFromToken(token));
        response.put("email", authService.getEmailFromToken(token));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/privileges")
    public ResponseEntity<Map<String, Object>> getPrivileges(@RequestHeader("X-User-Id") Long userId) {
        Map<String, Object> privileges = new HashMap<>();
        privileges.put("userId", userId);
        privileges.put("privileges", new String[]{"QRIS", "CASH_IN", "DASHBOARD"});
        return ResponseEntity.ok(privileges);
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Invalid authorization header");
    }
}
