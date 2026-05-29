package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.LoginRequest;
import com.attendance.dto.LoginResponse;
import com.attendance.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/admin-login")
    public ResponseEntity<ApiResponse<LoginResponse>> adminLogin(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.adminLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Admin login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        LoginResponse response = authService.getAuthenticatedUser(token);
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved", response));
    }
}
