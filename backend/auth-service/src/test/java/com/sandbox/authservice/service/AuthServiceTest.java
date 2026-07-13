package com.sandbox.authservice.service;

import com.sandbox.authservice.dto.LoginRequest;
import com.sandbox.authservice.dto.LoginResponse;
import com.sandbox.authservice.entity.Credential;
import com.sandbox.authservice.repository.CredentialRepository;
import com.sandbox.authservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private LoginRequest validLoginRequest;
    private LoginRequest invalidPasswordRequest;
    private Credential testCredential;
    private final String validToken = "valid.jwt.token";
    private final Long testUserId = 1L;
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail(testEmail);
        validLoginRequest.setPassword("correctPassword");

        invalidPasswordRequest = new LoginRequest();
        invalidPasswordRequest.setEmail(testEmail);
        invalidPasswordRequest.setPassword("wrongPassword");

        testCredential = new Credential();
        testCredential.setId(1L);
        testCredential.setUserId(testUserId);
        testCredential.setEmail(testEmail);
        testCredential.setPasswordHash(new BCryptPasswordEncoder().encode("correctPassword"));

        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);
    }

    @Test
    @DisplayName("login - should succeed with valid credentials")
    void login_Success() {
        when(credentialRepository.findByEmail(testEmail)).thenReturn(Optional.of(testCredential));
        when(jwtTokenProvider.generateToken(testUserId, testEmail)).thenReturn(validToken);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        LoginResponse response = authService.login(validLoginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(validToken);
        assertThat(response.getExpiresIn()).isEqualTo(86400L);

        verify(credentialRepository).findByEmail(testEmail);
        verify(jwtTokenProvider).generateToken(testUserId, testEmail);
        verify(valueOperations).set(eq("token:" + validToken), eq(testUserId.toString()), eq(86400000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("login - should throw exception when email not found")
    void login_EmailNotFound_ThrowsException() {
        when(credentialRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(validLoginRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid credentials");

        verify(credentialRepository).findByEmail(testEmail);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("login - should throw exception when password is wrong")
    void login_WrongPassword_ThrowsException() {
        when(credentialRepository.findByEmail(testEmail)).thenReturn(Optional.of(testCredential));

        assertThatThrownBy(() -> authService.login(invalidPasswordRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Invalid credentials");

        verify(credentialRepository).findByEmail(testEmail);
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    @DisplayName("verifyToken - should return true for valid token in cache")
    void verifyToken_ValidToken_ReturnsTrue() {
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:" + validToken)).thenReturn(testUserId.toString());

        boolean result = authService.verifyToken(validToken);

        assertThat(result).isTrue();
        verify(jwtTokenProvider).validateToken(validToken);
        verify(valueOperations).get("token:" + validToken);
    }

    @Test
    @DisplayName("verifyToken - should return false when token is invalid")
    void verifyToken_InvalidToken_ReturnsFalse() {
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(false);

        boolean result = authService.verifyToken(validToken);

        assertThat(result).isFalse();
        verify(jwtTokenProvider).validateToken(validToken);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    @DisplayName("verifyToken - should return false when token not in cache")
    void verifyToken_TokenNotInCache_ReturnsFalse() {
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("token:" + validToken)).thenReturn(null);

        boolean result = authService.verifyToken(validToken);

        assertThat(result).isFalse();
        verify(jwtTokenProvider).validateToken(validToken);
        verify(valueOperations).get("token:" + validToken);
    }

    @Test
    @DisplayName("getUserIdFromToken - should delegate to JwtTokenProvider")
    void getUserIdFromToken_Success() {
        when(jwtTokenProvider.getUserIdFromToken(validToken)).thenReturn(testUserId);

        Long userId = authService.getUserIdFromToken(validToken);

        assertThat(userId).isEqualTo(testUserId);
        verify(jwtTokenProvider).getUserIdFromToken(validToken);
    }

    @Test
    @DisplayName("getEmailFromToken - should delegate to JwtTokenProvider")
    void getEmailFromToken_Success() {
        when(jwtTokenProvider.getEmailFromToken(validToken)).thenReturn(testEmail);

        String email = authService.getEmailFromToken(validToken);

        assertThat(email).isEqualTo(testEmail);
        verify(jwtTokenProvider).getEmailFromToken(validToken);
    }
}
