package com.attendance.repository;

import com.attendance.entity.AdminCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminCredentialRepository extends JpaRepository<AdminCredential, Long> {

    Optional<AdminCredential> findByUsername(String username);

    Optional<AdminCredential> findByEmployeeId(Long employeeId);

    boolean existsByUsername(String username);
}
