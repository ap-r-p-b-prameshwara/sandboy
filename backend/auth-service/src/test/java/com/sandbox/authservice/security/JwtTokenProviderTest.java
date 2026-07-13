package com.sandbox.authservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private final Long testUserId = 1L;
    private final String testEmail = "test@example.com";
    private final String jwtSecret = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "environment", "production");
    }

    @Test
    @DisplayName("generateToken - should create valid JWT token")
    void generateToken_Success() {
        String token = jwtTokenProvider.generateToken(testUserId, testEmail);

        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken - should return true for valid token")
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtTokenProvider.generateToken(testUserId, testEmail);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken - should return false for malformed token")
    void validateToken_MalformedToken_ReturnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken("malformed.token.here");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken - should return false for empty token")
    void validateToken_EmptyToken_ReturnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("getUserIdFromToken - should extract userId correctly")
    void getUserIdFromToken_Success() {
        String token = jwtTokenProvider.generateToken(testUserId, testEmail);

        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("getEmailFromToken - should extract email correctly")
    void getEmailFromToken_Success() {
        String token = jwtTokenProvider.generateToken(testUserId, testEmail);

        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        assertThat(extractedEmail).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("getEnvironmentFromToken - should extract environment correctly")
    void getEnvironmentFromToken_Success() {
        String token = jwtTokenProvider.generateToken(testUserId, testEmail);

        String env = jwtTokenProvider.getEnvironmentFromToken(token);

        assertThat(env).isEqualTo("production");
    }

    @Test
    @DisplayName("generateToken - should use sandbox environment when configured")
    void generateToken_SandboxEnvironment() {
        ReflectionTestUtils.setField(jwtTokenProvider, "environment", "sandbox");

        String token = jwtTokenProvider.generateToken(testUserId, testEmail);

        assertThat(jwtTokenProvider.getEnvironmentFromToken(token)).isEqualTo("sandbox");
    }

    @Test
    @DisplayName("generateToken - different users get different tokens")
    void generateToken_DifferentUsers_DifferentTokens() {
        String token1 = jwtTokenProvider.generateToken(1L, "user1@example.com");
        String token2 = jwtTokenProvider.generateToken(2L, "user2@example.com");

        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtTokenProvider.getUserIdFromToken(token1)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getUserIdFromToken(token2)).isEqualTo(2L);
    }
}
