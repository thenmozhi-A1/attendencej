package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.PayrollSummaryDTO;
import com.attendance.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping
    public ResponseEntity<ApiResponse<PayrollSummaryDTO>> getMonthlyPayroll(
            @RequestParam(required = false) String month) {
        YearMonth payrollMonth = month != null && !month.isBlank() ? YearMonth.parse(month) : YearMonth.now();
        PayrollSummaryDTO payroll = payrollService.getMonthlyPayroll(payrollMonth);
        return ResponseEntity.ok(ApiResponse.success("Payroll calculated", payroll));
    }
}
