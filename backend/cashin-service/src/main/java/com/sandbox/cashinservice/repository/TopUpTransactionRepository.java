package com.sandbox.cashinservice.repository;

import com.sandbox.cashinservice.entity.TopUpTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopUpTransactionRepository extends JpaRepository<TopUpTransaction, Long> {
    List<TopUpTransaction> findByUserIdOrderByTransactionDateDesc(Long userId);
}
