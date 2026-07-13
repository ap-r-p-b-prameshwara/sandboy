package com.sandbox.qrisservice.repository;

import com.sandbox.qrisservice.entity.QrisTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrisTransactionRepository extends JpaRepository<QrisTransaction, Long> {
    List<QrisTransaction> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);
    Optional<QrisTransaction> findByTransactionId(String transactionId);
    boolean existsByTransactionId(String transactionId);
}
