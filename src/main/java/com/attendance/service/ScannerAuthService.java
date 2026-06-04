package com.attendance.service;

import com.attendance.dto.LoginResponse;
import com.attendance.entity.Employee;
import com.attendance.entity.ScannerCredential;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.exception.UnauthorizedException;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.ScannerCredentialRepository;
import com.attendance.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScannerAuthService {

    private final ScannerCredentialRepository scannerCredentialRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int QR_SALT_LENGTH = 32;

    @Transactional
    public Map<String, Object> generateQrCode(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        if (employee.getRole() != Employee.Role.ACCOUNTS && employee.getRole() != Employee.Role.ADMIN) {
            throw new BadRequestException("QR code scanner authentication is only available for ACCOUNTS employees");
        }

        if (!employee.getIsActive()) {
            throw new BadRequestException("Employee account is deactivated");
        }

        byte[] salt = new byte[QR_SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);

        String hashInput = employee.getId() + ":" + employee.getEmployeeCode() + ":" + System.currentTimeMillis();
        String qrCodeHash = hashWithSalt(hashInput, salt);

        ScannerCredential credential = scannerCredentialRepository.findByEmployeeId(employeeId)
                .map(existing -> {
                    existing.setQrCodeHash(qrCodeHash);
                    existing.setIsActive(true);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return existing;
                })
                .orElseGet(() -> ScannerCredential.builder()
                        .employee(employee)
                        .qrCodeHash(qrCodeHash)
                        .isActive(true)
                        .build());

        scannerCredentialRepository.save(credential);

        String qrData = Base64.getUrlEncoder().encodeToString(
                (employee.getId() + ":" + qrCodeHash).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", employeeId);
        response.put("employeeName", employee.getName());
        response.put("employeeCode", employee.getEmployeeCode());
        response.put("qrData", qrData);
        response.put("generatedAt", LocalDateTime.now().toString());

        return response;
    }

    @Transactional
    public LoginResponse verifyQrScan(String scannedData) {
        String decodedData;
        try {
            decodedData = new String(Base64.getUrlDecoder().decode(scannedData), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid QR code format");
        }

        String[] parts = decodedData.split(":", 2);
        if (parts.length != 2) {
            throw new BadRequestException("Invalid QR code data format");
        }

        Long employeeId;
        try {
            employeeId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid employee ID in QR code");
        }

        String qrCodeHash = parts[1];

        ScannerCredential credential = scannerCredentialRepository.findByQrCodeHash(qrCodeHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid QR code"));

        if (!credential.getEmployee().getId().equals(employeeId)) {
            throw new UnauthorizedException("QR code does not match employee");
        }

        if (!credential.getIsActive()) {
            throw new UnauthorizedException("QR code credential is deactivated");
        }

        Employee employee = credential.getEmployee();
        if (!employee.getIsActive()) {
            throw new UnauthorizedException("Employee account is deactivated");
        }

        byte[] newSalt = new byte[QR_SALT_LENGTH];
        SECURE_RANDOM.nextBytes(newSalt);
        String newHashInput = employee.getId() + ":" + employee.getEmployeeCode() + ":" + System.currentTimeMillis();
        String newQrCodeHash = hashWithSalt(newHashInput, newSalt);

        credential.setQrCodeHash(newQrCodeHash);
        credential.setUpdatedAt(LocalDateTime.now());
        scannerCredentialRepository.save(credential);

        String token = jwtUtil.generateToken(employee.getId(), employee.getEmail(), employee.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .employeeId(employee.getId())
                .role(employee.getRole().name())
                .name(employee.getName())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isScannerRegistered(Long employeeId) {
        return scannerCredentialRepository.existsByEmployeeId(employeeId);
    }

    @Transactional
    public void deactivateScanner(Long employeeId) {
        scannerCredentialRepository.findByEmployeeId(employeeId).ifPresent(credential -> {
            credential.setIsActive(false);
            credential.setUpdatedAt(LocalDateTime.now());
            scannerCredentialRepository.save(credential);
        });
    }

    private String hashWithSalt(String input, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[salt.length + hashBytes.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashBytes, 0, combined, salt.length, hashBytes.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash QR code data", e);
        }
    }
}
