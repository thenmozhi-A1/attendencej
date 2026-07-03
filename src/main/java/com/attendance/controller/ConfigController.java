package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.entity.Department;
import com.attendance.entity.Employee;
import com.attendance.entity.LeaveRequest;
import com.attendance.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final DepartmentRepository departmentRepository;

    @GetMapping("/form-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFormData() {
        Map<String, Object> data = new HashMap<>();
        
        // Fetch departments dynamically from the database
        List<String> departments = departmentRepository.findAll().stream()
                .map(Department::getName)
                .collect(Collectors.toList());
        data.put("departments", departments);

        // Fetch roles dynamically from the Enum
        List<String> roles = Arrays.stream(Employee.Role.values())
                .map(role -> role.name().toLowerCase())
                .collect(Collectors.toList());
        data.put("roles", roles);

        // Fetch leave types dynamically from the Enum
        List<String> leaveTypes = Arrays.stream(LeaveRequest.LeaveType.values())
                .map(LeaveRequest.LeaveType::name)
                .collect(Collectors.toList());
        data.put("leaveTypes", leaveTypes);

        return ResponseEntity.ok(ApiResponse.success("Form data retrieved successfully", data));
    }
}
