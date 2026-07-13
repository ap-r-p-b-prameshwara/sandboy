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

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final CredentialRepository credentialRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        Credential credential = credentialRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtTokenProvider.generateToken(credential.getUserId(), credential.getEmail());
        
        String tokenKey = "token:" + token;
        redisTemplate.opsForValue().set(tokenKey, credential.getUserId().toString(), jwtExpiration, TimeUnit.MILLISECONDS);

        log.info("Login successful for user: {}", credential.getUserId());
        return new LoginResponse(token, jwtExpiration / 1000);
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
