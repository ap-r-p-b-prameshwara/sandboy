package com.sandbox.userservice.service;

import com.sandbox.userservice.dto.RegistrationRequest;
import com.sandbox.userservice.dto.UserResponse;
import com.sandbox.userservice.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class DualWriteUserService {

    private final DataSource primaryDataSource;
    private final DataSource sandboxDataSource;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DualWriteUserService(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("sandboxDataSource") DataSource sandboxDataSource) {
        this.primaryDataSource = primaryDataSource;
        this.sandboxDataSource = sandboxDataSource;
    }

    @Transactional
    public UserResponse registerDualWrite(RegistrationRequest request) {
        log.info("Dual-write registration for: {}", request.getEmail());

        String passwordHash = passwordEncoder.encode(request.getPassword());
        LocalDateTime now = LocalDateTime.now();

        JdbcTemplate prodJdbc = new JdbcTemplate(primaryDataSource);
        JdbcTemplate sandboxJdbc = new JdbcTemplate(sandboxDataSource);

        String checkSql = "SELECT COUNT(*) FROM users.user WHERE email = ?";
        Integer count = prodJdbc.queryForObject(checkSql, Integer.class, request.getEmail());
        if (count != null && count > 0) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        String insertUserSql = """
            INSERT INTO users.user (email, password_hash, name, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """;

        Long prodUserId = prodJdbc.queryForObject(
            insertUserSql, Long.class,
            request.getEmail(), passwordHash, request.getName(), now, now
        );

        log.info("Inserted user in Production DB with id: {}", prodUserId);

        try {
            Long sandboxUserId = sandboxJdbc.queryForObject(
                insertUserSql, Long.class,
                request.getEmail(), passwordHash, request.getName(), now, now
            );
            log.info("Inserted user in Sandbox DB with id: {}", sandboxUserId);

            assignPrivileges(prodJdbc, prodUserId, new String[]{"CASH_IN", "DASHBOARD"});
            assignPrivileges(sandboxJdbc, sandboxUserId, new String[]{"QRIS", "CASH_IN", "DASHBOARD"});
            insertMerchant(sandboxJdbc, sandboxUserId);
            insertCredential(prodJdbc, prodUserId, request.getEmail(), passwordHash);
            insertCredential(sandboxJdbc, sandboxUserId, request.getEmail(), passwordHash);

        } catch (Exception e) {
            log.error("Failed to write to Sandbox DB, rolling back Production: {}", e.getMessage());
            prodJdbc.update("DELETE FROM users.user WHERE id = ?", prodUserId);
            throw new RuntimeException("Registration failed - sandbox DB unavailable", e);
        }

        return new UserResponse(prodUserId, request.getEmail(), request.getName());
    }

    private void insertCredential(JdbcTemplate jdbc, Long userId, String email, String passwordHash) {
        String insertCredSql = """
            INSERT INTO credentials.credential (user_id, email, password_hash, created_at)
            VALUES (?, ?, ?, ?)
            """;
        jdbc.update(insertCredSql, userId, email, passwordHash, LocalDateTime.now());
    }

    private void assignPrivileges(JdbcTemplate jdbc, Long userId, String[] features) {
        String insertPrivSql = """
            INSERT INTO privileges.privilege (user_id, feature, enabled, created_at, updated_at)
            VALUES (?, ?, true, ?, ?)
            """;
        LocalDateTime now = LocalDateTime.now();
        for (String feature : features) {
            jdbc.update(insertPrivSql, userId, feature, now, now);
        }
    }

    private void insertMerchant(JdbcTemplate jdbc, Long userId) {
        String insertMerchantSql = """
            INSERT INTO qris_merchants.merchants (user_id, merchant_name, nmid, is_active, daily_limit, created_at, updated_at)
            VALUES (?, ?, ?, true, ?, ?, ?)
            """;
        jdbc.update(insertMerchantSql, userId, "Sandbox Merchant", "SB" + userId + "0000", new BigDecimal("10000000"), LocalDateTime.now(), LocalDateTime.now());
    }
}
