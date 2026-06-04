package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.LoginResponse;
import com.attendance.service.ScannerAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/scanner")
@RequiredArgsConstructor
public class ScannerController {

    private final ScannerAuthService scannerAuthService;

    @GetMapping("/qr-code/{employeeId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateQrCode(@PathVariable Long employeeId) {
        Map<String, Object> qrData = scannerAuthService.generateQrCode(employeeId);
        return ResponseEntity.ok(ApiResponse.success("QR code generated", qrData));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyQrScan(@RequestBody Map<String, String> request) {
        String scannedData = request.get("scannedData");
        if (scannedData == null || scannedData.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("scannedData is required"));
        }
        LoginResponse response = scannerAuthService.verifyQrScan(scannedData);
        return ResponseEntity.ok(ApiResponse.success("QR scan verification successful", response));
    }

    @GetMapping("/status/{employeeId}")
    public ResponseEntity<ApiResponse<Boolean>> checkScannerStatus(@PathVariable Long employeeId) {
        boolean isRegistered = scannerAuthService.isScannerRegistered(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Scanner status checked", isRegistered));
    }

    @DeleteMapping("/deactivate/{employeeId}")
    public ResponseEntity<ApiResponse<Void>> deactivateScanner(@PathVariable Long employeeId) {
        scannerAuthService.deactivateScanner(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Scanner credential deactivated"));
    }
}
