package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.LeaveApprovalDTO;
import com.attendance.dto.LeaveRequestDTO;
import com.attendance.service.LeaveService;
import com.attendance.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;
    private final JwtUtil jwtUtil;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> createLeaveRequest(
            @Valid @RequestBody LeaveRequestDTO requestDTO) {
        LeaveRequestDTO created = leaveService.createLeaveRequest(requestDTO);
        return ResponseEntity.ok(ApiResponse.success("Leave request created", created));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> approveOrRejectLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveApprovalDTO approvalDTO,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long approverId = jwtUtil.extractEmployeeId(token);
        LeaveRequestDTO updated = leaveService.approveOrRejectLeave(id, approvalDTO, approverId);
        return ResponseEntity.ok(ApiResponse.success("Leave request updated", updated));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> getAllLeaves() {
        List<LeaveRequestDTO> leaves = leaveService.getAllLeaves();
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved", leaves));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> getLeavesByEmployee(
            @PathVariable Long employeeId) {
        List<LeaveRequestDTO> leaves = leaveService.getLeavesByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Employee leave requests retrieved", leaves));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> getLeavesByStatus(
            @PathVariable String status) {
        List<LeaveRequestDTO> leaves = leaveService.getLeavesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Leave requests by status retrieved", leaves));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> cancelLeaveRequest(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long employeeId = jwtUtil.extractEmployeeId(token);
        LeaveRequestDTO cancelled = leaveService.cancelLeaveRequest(id, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Leave request cancelled", cancelled));
    }
}
