package com.attendance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollDTO {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private YearMonth payrollMonth;
    private BigDecimal monthlySalary;
    private BigDecimal dailyRate;
    private Double leaveDays;
    private Double permissionDays;
    private Double absentDays;
    private Double deductionDays;
    private BigDecimal leaveDeduction;
    private BigDecimal permissionDeduction;
    private BigDecimal absentDeduction;
    private BigDecimal totalDeduction;
    private BigDecimal netSalary;
}
