package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.LoginResponse;
import com.attendance.dto.WebAuthnAuthenticationRequest;
import com.attendance.dto.WebAuthnRegistrationRequest;
import com.attendance.service.WebAuthnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webauthn")
@RequiredArgsConstructor
public class WebAuthnController {

    private final WebAuthnService webAuthnService;

    @PostMapping("/register/challenge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateRegistrationChallenge(
            @RequestParam Long employeeId) {
        Map<String, Object> challenge = webAuthnService.generateRegistrationChallenge(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Registration challenge generated", challenge));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyRegistration(
            @Valid @RequestBody WebAuthnRegistrationRequest request) {
        LoginResponse response = webAuthnService.verifyRegistration(request);
        return ResponseEntity.ok(ApiResponse.success("WebAuthn registration successful", response));
    }

    @PostMapping("/auth/challenge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateAuthenticationChallenge(
            @RequestParam Long employeeId) {
        Map<String, Object> challenge = webAuthnService.generateAuthenticationChallenge(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Authentication challenge generated", challenge));
    }

    @PostMapping("/auth/verify")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyAuthentication(
            @Valid @RequestBody WebAuthnAuthenticationRequest request) {
        LoginResponse response = webAuthnService.verifyAuthentication(request);
        return ResponseEntity.ok(ApiResponse.success("WebAuthn authentication successful", response));
    }

    @GetMapping("/status/{employeeId}")
    public ResponseEntity<ApiResponse<Boolean>> checkWebAuthnStatus(@PathVariable Long employeeId) {
        boolean isRegistered = webAuthnService.isWebAuthnRegistered(employeeId);
        return ResponseEntity.ok(ApiResponse.success("WebAuthn status checked", isRegistered));
    }

    @DeleteMapping("/credentials/{employeeId}")
    public ResponseEntity<ApiResponse<Void>> removeWebAuthnCredentials(@PathVariable Long employeeId) {
        webAuthnService.removeWebAuthnCredentials(employeeId);
        return ResponseEntity.ok(ApiResponse.success("WebAuthn credentials removed"));
    }
}
