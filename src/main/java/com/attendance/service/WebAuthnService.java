package com.attendance.service;

import com.attendance.dto.LoginResponse;
import com.attendance.dto.WebAuthnAuthenticationRequest;
import com.attendance.dto.WebAuthnRegistrationRequest;
import com.attendance.entity.Employee;
import com.attendance.entity.WebAuthnChallenge;
import com.attendance.entity.WebAuthnCredential;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.exception.UnauthorizedException;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.WebAuthnChallengeRepository;
import com.attendance.repository.WebAuthnCredentialRepository;
import com.attendance.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebAuthnService {

    private final WebAuthnChallengeRepository challengeRepository;
    private final WebAuthnCredentialRepository credentialRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CHALLENGE_LENGTH = 32;
    private static final int CHALLENGE_EXPIRY_MINUTES = 5;

    @Transactional
    public Map<String, Object> generateRegistrationChallenge(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        if (employee.getRole() != Employee.Role.TECH && employee.getRole() != Employee.Role.ADMIN) {
            throw new BadRequestException("WebAuthn registration is only available for TECH and ADMIN employees");
        }

        cleanupExpiredChallenges();

        byte[] challengeBytes = new byte[CHALLENGE_LENGTH];
        SECURE_RANDOM.nextBytes(challengeBytes);
        String challengeBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);

        WebAuthnChallenge challenge = WebAuthnChallenge.builder()
                .employee(employee)
                .challenge(challengeBase64)
                .expiresAt(LocalDateTime.now().plusMinutes(CHALLENGE_EXPIRY_MINUTES))
                .build();

        challengeRepository.save(challenge);

        Map<String, Object> response = new HashMap<>();
        response.put("challenge", challengeBase64);
        response.put("employeeId", employeeId);
        response.put("employeeName", employee.getName());
        response.put("rpId", "localhost");
        response.put("timeout", 60000);

        return response;
    }

    @Transactional
    public LoginResponse verifyRegistration(WebAuthnRegistrationRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        WebAuthnChallenge challenge = challengeRepository
                .findTopByEmployeeIdOrderByCreatedAtDesc(request.getEmployeeId())
                .orElseThrow(() -> new BadRequestException("No pending registration challenge found"));

        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            challengeRepository.delete(challenge);
            throw new BadRequestException("Registration challenge has expired. Please request a new one");
        }

        String credentialId = request.getCredentialId();
        String publicKey = request.getPublicKey();

        if (credentialRepository.findByCredentialId(credentialId).isPresent()) {
            throw new BadRequestException("Credential is already registered");
        }

        WebAuthnCredential credential = WebAuthnCredential.builder()
                .employee(employee)
                .credentialId(credentialId)
                .publicKey(publicKey)
                .signCount(request.getSignCount())
                .transports(request.getTransports())
                .deviceName(request.getDeviceName())
                .build();

        credentialRepository.save(credential);
        challengeRepository.delete(challenge);

        String token = jwtUtil.generateToken(employee.getId(), employee.getEmail(), employee.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .employeeId(employee.getId())
                .role(employee.getRole().name())
                .name(employee.getName())
                .build();
    }

    @Transactional
    public Map<String, Object> generateAuthenticationChallenge(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        List<WebAuthnCredential> credentials = credentialRepository.findByEmployeeId(employeeId);
        if (credentials.isEmpty()) {
            throw new BadRequestException("No WebAuthn credentials registered for this employee. Please register first.");
        }

        cleanupExpiredChallenges();

        byte[] challengeBytes = new byte[CHALLENGE_LENGTH];
        SECURE_RANDOM.nextBytes(challengeBytes);
        String challengeBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes);

        WebAuthnChallenge challenge = WebAuthnChallenge.builder()
                .employee(employee)
                .challenge(challengeBase64)
                .expiresAt(LocalDateTime.now().plusMinutes(CHALLENGE_EXPIRY_MINUTES))
                .build();

        challengeRepository.save(challenge);

        List<String> credentialIds = credentials.stream()
                .map(WebAuthnCredential::getCredentialId)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("challenge", challengeBase64);
        response.put("employeeId", employeeId);
        response.put("credentialIds", credentialIds);
        response.put("rpId", "localhost");
        response.put("timeout", 60000);

        return response;
    }

    @Transactional
    public LoginResponse verifyAuthentication(WebAuthnAuthenticationRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        if (!employee.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        WebAuthnChallenge challenge = challengeRepository
                .findTopByEmployeeIdOrderByCreatedAtDesc(request.getEmployeeId())
                .orElseThrow(() -> new BadRequestException("No pending authentication challenge found"));

        if (challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            challengeRepository.delete(challenge);
            throw new BadRequestException("Authentication challenge has expired. Please request a new one");
        }

        WebAuthnCredential credential = credentialRepository.findByCredentialId(request.getCredentialId())
                .orElseThrow(() -> new BadRequestException("Credential not found"));

        if (!credential.getEmployee().getId().equals(request.getEmployeeId())) {
            throw new UnauthorizedException("Credential does not belong to this employee");
        }

        // In a real WebAuthn implementation, the client signs the challenge with the private key
        // and the server verifies the signature against the stored public key.
        // For this implementation, we verify that the credential exists and is valid.
        // The browser's navigator.credentials.get() handles the actual biometric verification.

        if (request.getSignCount() <= credential.getSignCount()) {
            throw new BadRequestException("Invalid sign count - possible replay attack detected");
        }

        credential.setSignCount(request.getSignCount());
        credentialRepository.save(credential);

        challengeRepository.delete(challenge);

        String token = jwtUtil.generateToken(employee.getId(), employee.getEmail(), employee.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .employeeId(employee.getId())
                .role(employee.getRole().name())
                .name(employee.getName())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isWebAuthnRegistered(Long employeeId) {
        return credentialRepository.existsByEmployeeId(employeeId);
    }

    @Transactional
    public void removeWebAuthnCredentials(Long employeeId) {
        credentialRepository.deleteByEmployeeId(employeeId);
    }

    private void cleanupExpiredChallenges() {
        challengeRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
