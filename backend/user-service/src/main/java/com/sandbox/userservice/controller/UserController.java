package com.sandbox.userservice.controller;

import com.sandbox.userservice.dto.PrivilegeResponse;
import com.sandbox.userservice.dto.RegistrationRequest;
import com.sandbox.userservice.dto.UserResponse;
import com.sandbox.userservice.service.UserService;
import com.sandbox.userservice.service.DualWriteUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DualWriteUserService dualWriteUserService;

    @Value("${spring.profiles.active:prod}")
    private String activeProfile;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegistrationRequest request) {
        UserResponse response;
        if ("prod".equals(activeProfile)) {
            response = dualWriteUserService.registerDualWrite(request);
        } else {
            response = userService.register(request);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestHeader("X-User-Id") Long userId) {
        UserResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(userId, request.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/privileges")
    public ResponseEntity<PrivilegeResponse> getPrivileges(@RequestHeader("X-User-Id") Long userId) {
        PrivilegeResponse response = userService.getPrivileges(userId);
        return ResponseEntity.ok(response);
    }

    public static class UpdateProfileRequest {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
