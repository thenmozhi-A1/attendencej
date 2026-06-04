package com.attendance.repository;

import com.attendance.entity.WebAuthnChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebAuthnChallengeRepository extends JpaRepository<WebAuthnChallenge, Long> {

    Optional<WebAuthnChallenge> findTopByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<WebAuthnChallenge> findByEmployeeId(Long employeeId);

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
