package com.attendance.controller;

import com.attendance.dto.ApiResponse;
import com.attendance.dto.EmployeeDTO;
import com.attendance.entity.Employee;
import com.attendance.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getAllEmployees() {
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved", employees));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getActiveEmployees() {
        List<EmployeeDTO> employees = employeeService.getActiveEmployees();
        return ResponseEntity.ok(ApiResponse.success("Active employees retrieved", employees));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeById(@PathVariable Long id) {
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved", employee));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployeesByRole(@PathVariable String role) {
        Employee.Role employeeRole = Employee.Role.valueOf(role.toUpperCase());
        List<EmployeeDTO> employees = employeeService.getEmployeesByRole(employeeRole);
        return ResponseEntity.ok(ApiResponse.success("Employees by role retrieved", employees));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        List<EmployeeDTO> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Employees by department retrieved", employees));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeDTO>> createEmployee(@Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO created = employeeService.createEmployee(employeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(@PathVariable Long id,
                                                                    @Valid @RequestBody EmployeeDTO employeeDTO) {
        EmployeeDTO updated = employeeService.updateEmployee(id, employeeDTO);
        return ResponseEntity.ok(ApiResponse.success("Employee updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated"));
    }
}
