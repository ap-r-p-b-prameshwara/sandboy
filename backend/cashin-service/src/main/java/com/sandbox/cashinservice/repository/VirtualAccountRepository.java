package com.sandbox.cashinservice.repository;

import com.sandbox.cashinservice.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Long> {
    List<VirtualAccount> findByUserId(Long userId);
    List<VirtualAccount> findByUserIdAndIsActiveTrue(Long userId);
}
