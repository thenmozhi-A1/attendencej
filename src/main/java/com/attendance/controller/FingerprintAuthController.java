package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
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
        Cookie cookie = new Cookie("SESSION", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1 day
        cookie.setSameSite("Lax");
        response.addCookie(cookie);
        // Build success response (could include user info)
        return ResponseEntity.ok(ApiResponse.success("Fingerprint verified", true));
    }
}

// Simple request DTO for fingerprint data
class FingerprintRequest {
    private String credential;
    public String getCredential() { return credential; }
    public void setCredential(String credential) { this.credential = credential; }
}
