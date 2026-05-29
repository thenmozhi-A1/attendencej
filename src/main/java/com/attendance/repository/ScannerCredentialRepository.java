package com.attendance.repository;

import com.attendance.entity.ScannerCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScannerCredentialRepository extends JpaRepository<ScannerCredential, Long> {

    Optional<ScannerCredential> findByEmployeeId(Long employeeId);

    Optional<ScannerCredential> findByQrCodeHash(String qrCodeHash);

    boolean existsByEmployeeId(Long employeeId);
}
