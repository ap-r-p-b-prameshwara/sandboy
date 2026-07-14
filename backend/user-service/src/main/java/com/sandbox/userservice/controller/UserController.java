package com.sandbox.userservice.controller;

import com.sandbox.userservice.dto.PrivilegeResponse;
import com.sandbox.userservice.dto.RegistrationRequest;
import com.sandbox.userservice.dto.UserResponse;
import com.sandbox.userservice.entity.Credential;
import com.sandbox.userservice.entity.Privilege;
import com.sandbox.userservice.entity.User;
import com.sandbox.userservice.repository.CredentialRepository;
import com.sandbox.userservice.repository.PrivilegeRepository;
import com.sandbox.userservice.repository.UserRepository;
import com.sandbox.userservice.service.UserService;
import com.sandbox.userservice.service.DualWriteUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DualWriteUserService dualWriteUserService;
    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final PrivilegeRepository privilegeRepository;

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

    @GetMapping("/users/{email}/sync-data")
    public ResponseEntity<Map<String, Object>> getSyncData(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Credential credential = credentialRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Credential not found"));

        List<Privilege> privileges = privilegeRepository.findByUserId(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("passwordHash", user.getPasswordHash());
        response.put("name", user.getName());
        response.put("credential", Map.of(
            "email", credential.getEmail(),
            "passwordHash", credential.getPasswordHash()
        ));
        response.put("privileges", privileges.stream()
            .map(p -> Map.of("feature", p.getFeature()))
            .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    public static class UpdateProfileRequest {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
