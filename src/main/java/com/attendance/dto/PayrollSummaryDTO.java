package com.attendance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollSummaryDTO {

    private YearMonth payrollMonth;
    private Integer daysInMonth;
    private Integer employeeCount;
    private BigDecimal grossSalary;
    private BigDecimal totalDeductions;
    private BigDecimal netPayable;
    private List<PayrollDTO> payrolls;
}
