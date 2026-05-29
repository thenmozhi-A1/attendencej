package com.attendance.repository;

import com.attendance.entity.WebAuthnCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebAuthnCredentialRepository extends JpaRepository<WebAuthnCredential, Long> {

    List<WebAuthnCredential> findByEmployeeId(Long employeeId);

    Optional<WebAuthnCredential> findByCredentialId(String credentialId);

    boolean existsByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}
