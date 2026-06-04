package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.AttendanceDTO;
import com.attendance.dto.CheckInRequest;
import com.attendance.dto.CheckOutRequest;
import com.attendance.dto.DashboardStats;
import com.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
<<<<<<< HEAD
    private final com.attendance.util.JwtUtil jwtUtil;

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceDTO>> checkIn(
            @Valid @RequestBody CheckInRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // If an Authorization header with a valid JWT is provided, extract the employee id
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long tokenEmployeeId = jwtUtil.extractEmployeeId(token);
                if (tokenEmployeeId != null) {
                    request.setEmployeeId(tokenEmployeeId);
                    request.setWebAuthnAuthenticated(true);
                }
            }
        }

=======

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceDTO>> checkIn(@Valid @RequestBody CheckInRequest request) {
>>>>>>> a51b55306b65905891f055e408b3cf0c90e98396
        AttendanceDTO response = attendanceService.checkIn(request);
        return ResponseEntity.ok(ApiResponse.success("Check-in successful", response));
    }

    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceDTO>> checkOut(@Valid @RequestBody CheckOutRequest request) {
        AttendanceDTO response = attendanceService.checkOut(request);
        return ResponseEntity.ok(ApiResponse.success("Check-out successful", response));
    }

    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getAttendanceRecords(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AttendanceDTO> records = attendanceService.getAttendanceRecords(employeeId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Attendance records retrieved", records));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<AttendanceDTO>>> getTodayAttendance() {
        List<AttendanceDTO> records = attendanceService.getTodayAttendance();
        return ResponseEntity.ok(ApiResponse.success("Today's attendance retrieved", records));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        DashboardStats stats = attendanceService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", stats));
    }
}
