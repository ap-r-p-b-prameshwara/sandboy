package com.sandbox.userservice.controller;

import com.sandbox.userservice.entity.Credential;
import com.sandbox.userservice.entity.Privilege;
import com.sandbox.userservice.entity.User;
import com.sandbox.userservice.repository.CredentialRepository;
import com.sandbox.userservice.repository.PrivilegeRepository;
import com.sandbox.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final UserRepository userRepository;
    private final PrivilegeRepository privilegeRepository;
    private final CredentialRepository credentialRepository;
    private final RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Value("${user.service.prod.url:http://user-service-prod:8081}")
    private String prodServiceUrl;

    public SyncController(UserRepository userRepository,
                          PrivilegeRepository privilegeRepository,
                          CredentialRepository credentialRepository,
                          DataSource dataSource) {
        this.userRepository = userRepository;
        this.privilegeRepository = privilegeRepository;
        this.credentialRepository = credentialRepository;
        this.restTemplate = new RestTemplate();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> syncUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.ok(Map.of("status", "already_exists"));
        }

        try {
            // Fetch sync data from production user-service
            @SuppressWarnings("unchecked")
            Map<String, Object> syncData = restTemplate.getForObject(
                prodServiceUrl + "/api/users/" + email + "/sync-data",
                Map.class
            );

            if (syncData == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch sync data"));
            }

            // Insert user
            User user = new User();
            user.setEmail((String) syncData.get("email"));
            user.setPasswordHash((String) syncData.get("passwordHash"));
            user.setName((String) syncData.get("name"));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            User savedUser = userRepository.save(user);

            // Insert credential
            @SuppressWarnings("unchecked")
            Map<String, String> credData = (Map<String, String>) syncData.get("credential");
            if (credData != null) {
                Credential credential = new Credential();
                credential.setUserId(savedUser.getId());
                credential.setEmail(credData.get("email"));
                credential.setPasswordHash(credData.get("passwordHash"));
                credential.setCreatedAt(LocalDateTime.now());
                credentialRepository.save(credential);
            }

            // Insert privileges
            @SuppressWarnings("unchecked")
            List<Map<String, String>> privData = (List<Map<String, String>>) syncData.get("privileges");
            if (privData != null) {
                for (Map<String, String> p : privData) {
                    Privilege privilege = new Privilege();
                    privilege.setUserId(savedUser.getId());
                    privilege.setFeature(p.get("feature"));
                    privilege.setEnabled(true);
                    privilege.setCreatedAt(LocalDateTime.now());
                    privilege.setUpdatedAt(LocalDateTime.now());
                    privilegeRepository.save(privilege);
                }
            }

            String insertMerchantSql = """
                INSERT INTO qris_merchants.merchants (user_id, merchant_name, nmid, is_active, daily_limit, created_at, updated_at)
                VALUES (?, ?, ?, true, ?, ?, ?)
                """;
            jdbcTemplate.update(insertMerchantSql, savedUser.getId(), "Sandbox Merchant", "SB" + savedUser.getId() + "0000", new BigDecimal("10000000"), LocalDateTime.now(), LocalDateTime.now());

            return ResponseEntity.ok(Map.of("status", "synced", "userId", savedUser.getId()));

        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found in production"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Sync failed: " + e.getMessage()));
        }
    }
}
