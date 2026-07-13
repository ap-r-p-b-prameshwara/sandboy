package com.sandbox.userservice.service;

import com.sandbox.userservice.dto.PrivilegeResponse;
import com.sandbox.userservice.dto.RegistrationRequest;
import com.sandbox.userservice.dto.UserResponse;
import com.sandbox.userservice.entity.Privilege;
import com.sandbox.userservice.entity.User;
import com.sandbox.userservice.repository.PrivilegeRepository;
import com.sandbox.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PrivilegeRepository privilegeRepository;

    @InjectMocks
    private UserService userService;

    private RegistrationRequest registrationRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setName("Test User");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setName("Test User");
    }

    @Test
    @DisplayName("register - should successfully register new user")
    void register_Success() {
        // Given
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(privilegeRepository.save(any(Privilege.class))).thenReturn(new Privilege());

        // When
        UserResponse response = userService.register(registrationRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getName()).isEqualTo("Test User");

        verify(userRepository).existsByEmail(registrationRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(privilegeRepository, times(3)).save(any(Privilege.class));
    }

    @Test
    @DisplayName("register - should throw exception when email already exists")
    void register_EmailAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.register(registrationRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Email already registered");

        verify(userRepository).existsByEmail(registrationRequest.getEmail());
        verify(userRepository, never()).save(any());
        verify(privilegeRepository, never()).save(any());
    }

    @Test
    @DisplayName("register - should assign default privileges")
    void register_AssignsDefaultPrivileges() {
        // Given
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.register(registrationRequest);

        // Then
        ArgumentCaptor<Privilege> privilegeCaptor = ArgumentCaptor.forClass(Privilege.class);
        verify(privilegeRepository, times(3)).save(privilegeCaptor.capture());

        List<Privilege> savedPrivileges = privilegeCaptor.getAllValues();
        assertThat(savedPrivileges).hasSize(3);
        assertThat(savedPrivileges.get(0).getFeature()).isEqualTo("QRIS");
        assertThat(savedPrivileges.get(1).getFeature()).isEqualTo("CASH_IN");
        assertThat(savedPrivileges.get(2).getFeature()).isEqualTo("DASHBOARD");
    }

    @Test
    @DisplayName("getProfile - should return user profile")
    void getProfile_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.getProfile(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("getProfile - should throw exception when user not found")
    void getProfile_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getProfile(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("updateProfile - should update user name")
    void updateProfile_Success() {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("test@example.com");
        updatedUser.setName("Updated Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserResponse response = userService.updateProfile(1L, "Updated Name");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Name");

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile - should throw exception when user not found")
    void updateProfile_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateProfile(999L, "New Name"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPrivileges - should return user privileges")
    void getPrivileges_Success() {
        // Given
        Privilege privilege1 = new Privilege();
        privilege1.setUserId(1L);
        privilege1.setFeature("QRIS");
        privilege1.setEnabled(true);

        Privilege privilege2 = new Privilege();
        privilege2.setUserId(1L);
        privilege2.setFeature("CASH_IN");
        privilege2.setEnabled(true);

        when(privilegeRepository.findByUserId(1L)).thenReturn(Arrays.asList(privilege1, privilege2));

        // When
        PrivilegeResponse response = userService.getPrivileges(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getPrivileges()).hasSize(2);

        verify(privilegeRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("getPrivileges - should return empty list when no privileges")
    void getPrivileges_NoPrivileges_ReturnsEmptyList() {
        // Given
        when(privilegeRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        // When
        PrivilegeResponse response = userService.getPrivileges(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPrivileges()).isEmpty();

        verify(privilegeRepository).findByUserId(1L);
    }
}
