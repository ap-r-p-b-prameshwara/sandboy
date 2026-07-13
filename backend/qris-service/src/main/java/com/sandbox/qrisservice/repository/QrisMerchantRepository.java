package com.sandbox.qrisservice.repository;

import com.sandbox.qrisservice.entity.QrisMerchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QrisMerchantRepository extends JpaRepository<QrisMerchant, Long> {
    Optional<QrisMerchant> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    boolean existsByNmid(String nmid);
}
