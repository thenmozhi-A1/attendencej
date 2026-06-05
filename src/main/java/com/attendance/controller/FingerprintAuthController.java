package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.TechLoginRequest;
import com.attendance.service.FingerprintAuthService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class FingerprintAuthController {

    private final FingerprintAuthService fingerprintAuthService;

    public FingerprintAuthController(FingerprintAuthService fingerprintAuthService) {
        this.fingerprintAuthService = fingerprintAuthService;
    }

    @PostMapping("/fingerprint")
    public ResponseEntity<ApiResponse<?>> verifyFingerprint(@RequestBody TechLoginRequest request, HttpServletResponse response) {
        // Validate input
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getCredential())) {
            return ResponseEntity.status(400).body(ApiResponse.error("Invalid request"));
        }
        // Verify fingerprint via service
        boolean verified = fingerprintAuthService.verifyFingerprint(request);
        if (!verified) {
            return ResponseEntity.status(400).body(ApiResponse.error("Fingerprint verification failed"));
        }
        // Generate session token (placeholder)
        String token = UUID.randomUUID().toString();
        // Set HttpOnly, Secure, SameSite cookie
        ResponseCookie cookie = ResponseCookie.from("SESSION", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        // Build success response (could include user info)
        return ResponseEntity.ok(ApiResponse.success("Fingerprint verified", true));
    }
}
