package com.attendance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {

    private Long id;
    private String employeeCode;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private Long departmentId;
    private String department;
    private String departmentName;
    private BigDecimal monthlySalary;
    private Boolean isActive;
    private String status;
    private LocalDate hireDate;
    private LocalDateTime createdAt;
}
