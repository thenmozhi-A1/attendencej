package com.attendance.service;

import com.attendance.dto.PayrollDTO;
import com.attendance.dto.PayrollSummaryDTO;
import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.Employee;
import com.attendance.entity.LeaveRequest;
import com.attendance.repository.AttendanceRecordRepository;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    @Transactional(readOnly = true)
    public PayrollSummaryDTO getMonthlyPayroll(YearMonth month) {
        YearMonth payrollMonth = month != null ? month : YearMonth.now();
        LocalDate startDate = payrollMonth.atDay(1);
        LocalDate endDate = payrollMonth.atEndOfMonth();
        int daysInMonth = payrollMonth.lengthOfMonth();

        List<PayrollDTO> payrolls = employeeRepository.findByIsActiveTrue().stream()
                .map(employee -> calculateEmployeePayroll(employee, payrollMonth, startDate, endDate, daysInMonth))
                .toList();

        BigDecimal grossSalary = sum(payrolls.stream().map(PayrollDTO::getMonthlySalary).toList());
        BigDecimal totalDeductions = sum(payrolls.stream().map(PayrollDTO::getTotalDeduction).toList());
        BigDecimal netPayable = sum(payrolls.stream().map(PayrollDTO::getNetSalary).toList());

        return PayrollSummaryDTO.builder()
                .payrollMonth(payrollMonth)
                .daysInMonth(daysInMonth)
                .employeeCount(payrolls.size())
                .grossSalary(grossSalary)
                .totalDeductions(totalDeductions)
                .netPayable(netPayable)
                .payrolls(payrolls)
                .build();
    }

    private PayrollDTO calculateEmployeePayroll(
            Employee employee,
            YearMonth payrollMonth,
            LocalDate startDate,
            LocalDate endDate,
            int daysInMonth) {
        BigDecimal monthlySalary = employee.getMonthlySalary() != null ? employee.getMonthlySalary() : BigDecimal.ZERO;
        BigDecimal dailyRate = monthlySalary.divide(BigDecimal.valueOf(daysInMonth), 2, RoundingMode.HALF_UP);

        double leaveDays = calculateApprovedLeaveDays(employee.getId(), startDate, endDate);
        List<AttendanceRecord> records = attendanceRecordRepository.findByEmployeeIdAndDateBetween(
                employee.getId(), startDate, endDate);
        double absentDays = records.stream()
                .filter(record -> record.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                .count();
        double permissionDays = records.stream()
                .filter(record -> record.getStatus() == AttendanceRecord.AttendanceStatus.HALF_DAY)
                .count() * 0.5;

        BigDecimal leaveDeduction = dailyRate.multiply(BigDecimal.valueOf(leaveDays)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal permissionDeduction = dailyRate.multiply(BigDecimal.valueOf(permissionDays)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal absentDeduction = dailyRate.multiply(BigDecimal.valueOf(absentDays)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDeduction = leaveDeduction.add(permissionDeduction).add(absentDeduction);
        BigDecimal netSalary = monthlySalary.subtract(totalDeduction).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        return PayrollDTO.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(employee.getName())
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .payrollMonth(payrollMonth)
                .monthlySalary(monthlySalary.setScale(2, RoundingMode.HALF_UP))
                .dailyRate(dailyRate)
                .leaveDays(leaveDays)
                .permissionDays(permissionDays)
                .absentDays(absentDays)
                .deductionDays(leaveDays + permissionDays + absentDays)
                .leaveDeduction(leaveDeduction)
                .permissionDeduction(permissionDeduction)
                .absentDeduction(absentDeduction)
                .totalDeduction(totalDeduction.setScale(2, RoundingMode.HALF_UP))
                .netSalary(netSalary)
                .build();
    }

    private double calculateApprovedLeaveDays(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return leaveRequestRepository
                .findByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employeeId, LeaveRequest.LeaveStatus.APPROVED, endDate, startDate)
                .stream()
                .mapToLong(leave -> {
                    LocalDate effectiveStart = leave.getStartDate().isBefore(startDate) ? startDate : leave.getStartDate();
                    LocalDate effectiveEnd = leave.getEndDate().isAfter(endDate) ? endDate : leave.getEndDate();
                    return ChronoUnit.DAYS.between(effectiveStart, effectiveEnd) + 1;
                })
                .sum();
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
