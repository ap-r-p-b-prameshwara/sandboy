package com.sandbox.userservice.repository;

import com.sandbox.userservice.entity.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Long> {
    List<Privilege> findByUserId(Long userId);
    boolean existsByUserIdAndFeature(Long userId, String feature);
    void deleteByUserId(Long userId);
}
