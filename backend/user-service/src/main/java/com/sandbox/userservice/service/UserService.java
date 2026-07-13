package com.sandbox.userservice.service;

import com.sandbox.userservice.dto.PrivilegeResponse;
import com.sandbox.userservice.dto.RegistrationRequest;
import com.sandbox.userservice.dto.UserResponse;
import com.sandbox.userservice.entity.Privilege;
import com.sandbox.userservice.entity.User;
import com.sandbox.userservice.repository.PrivilegeRepository;
import com.sandbox.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PrivilegeRepository privilegeRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserResponse register(RegistrationRequest request) {
        log.info("Registering user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());

        User savedUser = userRepository.save(user);
        
        assignDefaultPrivileges(savedUser.getId());

        log.info("User registered successfully with id: {}", savedUser.getId());
        return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getName());
    }

    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }

    @Transactional
    public UserResponse updateProfile(Long userId, String name) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setName(name);
        User updatedUser = userRepository.save(user);
        return new UserResponse(updatedUser.getId(), updatedUser.getEmail(), updatedUser.getName());
    }

    public PrivilegeResponse getPrivileges(Long userId) {
        List<Privilege> privileges = privilegeRepository.findByUserId(userId);
        List<PrivilegeResponse.FeaturePrivilege> featurePrivileges = privileges.stream()
            .map(p -> new PrivilegeResponse.FeaturePrivilege(p.getFeature(), p.getEnabled()))
            .collect(Collectors.toList());
        return new PrivilegeResponse(userId, featurePrivileges);
    }

    private void assignDefaultPrivileges(Long userId) {
        String[] defaultFeatures = {"QRIS", "CASH_IN", "DASHBOARD"};
        for (String feature : defaultFeatures) {
            Privilege privilege = new Privilege();
            privilege.setUserId(userId);
            privilege.setFeature(feature);
            privilege.setEnabled(true);
            privilegeRepository.save(privilege);
        }
    }
}
