package com.sandbox.authservice.service;

import com.sandbox.authservice.dto.LoginRequest;
import com.sandbox.authservice.dto.LoginResponse;
import com.sandbox.authservice.entity.Credential;
import com.sandbox.authservice.repository.CredentialRepository;
import com.sandbox.authservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CredentialRepository credentialRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    @Value("${jwt.environment:production}")
    private String jwtEnvironment;

    @Value("${user.service.sandbox.url:http://user-service-sandbox:8082}")
    private String sandboxServiceUrl;

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for: {} on env: {}", request.getEmail(), jwtEnvironment);

        try {
            Credential credential = credentialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Not found in sandbox"));

            // Sandbox: skip password validation, langsung generate token
            if (!"sandbox".equals(jwtEnvironment)) {
                if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
                    throw new RuntimeException("Invalid credentials");
                }
            }

            String token = jwtTokenProvider.generateToken(credential.getUserId(), credential.getEmail(), jwtEnvironment);

            String tokenKey = "token:" + token;
            redisTemplate.opsForValue().set(tokenKey, credential.getUserId().toString(), jwtExpiration, TimeUnit.MILLISECONDS);

            log.info("Login successful for user: {} on env: {}", credential.getUserId(), jwtEnvironment);
            return new LoginResponse(token, null, jwtExpiration / 1000);

        } catch (RuntimeException e) {
            if ("sandbox".equals(jwtEnvironment) && e.getMessage().contains("Not found in sandbox")) {
                log.info("User not found in sandbox, attempting sync from production: {}", request.getEmail());

                try {
                    Map<String, String> syncRequest = new HashMap<>();
                    syncRequest.put("email", request.getEmail());
                    restTemplate.postForEntity(sandboxServiceUrl + "/api/sync", syncRequest, String.class);
                    log.info("Sync completed for: {}", request.getEmail());
                } catch (Exception syncError) {
                    log.error("Sync failed for: {}", request.getEmail(), syncError);
                    throw new RuntimeException("Sync failed: unable to sync user from production");
                }

                Credential credential = credentialRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Sync completed but credential not found"));

                String token = jwtTokenProvider.generateToken(credential.getUserId(), credential.getEmail(), jwtEnvironment);
                String tokenKey = "token:" + token;
                redisTemplate.opsForValue().set(tokenKey, credential.getUserId().toString(), jwtExpiration, TimeUnit.MILLISECONDS);

                log.info("Login successful after sync for user: {} on env: {}", credential.getUserId(), jwtEnvironment);
                return new LoginResponse(token, null, jwtExpiration / 1000);
            }
            throw e;
        }
    }

    public boolean verifyToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            return false;
        }

        String tokenKey = "token:" + token;
        String cachedUserId = redisTemplate.opsForValue().get(tokenKey);
        
        return cachedUserId != null;
    }

    public Long getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    public String getEmailFromToken(String token) {
        return jwtTokenProvider.getEmailFromToken(token);
    }
}
