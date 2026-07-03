package com.attendance.service;

import com.attendance.dto.LoginRequest;
import com.attendance.dto.LoginResponse;
import com.attendance.entity.Employee;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.UnauthorizedException;
import com.attendance.repository.EmployeeRepository;
import com.attendance.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        
        Employee employee = employeeRepository.findByEmployeeCode(username)
                .orElseGet(() -> employeeRepository.findByEmail(username)
                        .orElseThrow(() -> new UnauthorizedException("Invalid username or password")));

        if (!passwordEncoder.matches(request.getPassword(), employee.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        if (!employee.getIsActive()) {
            throw new BadRequestException("Account is deactivated");
        }

        String token = jwtUtil.generateToken(employee.getId(), employee.getEmployeeCode(), employee.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .employeeId(employee.getId())
                .role(employee.getRole().name())
                .name(employee.getName())
                .firstName(employee.getName() != null ? employee.getName().split(" ")[0] : "User")
                .employeeCode(employee.getEmployeeCode())
                .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .email(employee.getEmail())
                .monthlySalary(employee.getMonthlySalary())
                .build();
    }

    public LoginResponse getAuthenticatedUser(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        String username = jwtUtil.extractUsername(token);
        Long employeeId = jwtUtil.extractEmployeeId(token);
        String role = jwtUtil.extractRole(token);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new UnauthorizedException("Employee not found"));

        return LoginResponse.builder()
                .token(token)
                .employeeId(employeeId)
                .role(role)
                .name(employee.getName())
                .firstName(employee.getName() != null ? employee.getName().split(" ")[0] : "User")
                .employeeCode(employee.getEmployeeCode())
                .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .email(employee.getEmail())
                .monthlySalary(employee.getMonthlySalary())
                .build();
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
