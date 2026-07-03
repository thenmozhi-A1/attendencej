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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Set<LocalDate> approvedLeaveDates = getApprovedLeaveDates(employee.getId(), startDate, endDate);

        List<AttendanceRecord> records = attendanceRecordRepository.findByEmployeeIdAndDateBetween(
                employee.getId(), startDate, endDate);
                
        double manualLeaveDays = records.stream()
                .filter(record -> record.getStatus() == AttendanceRecord.AttendanceStatus.ON_LEAVE)
                .filter(record -> !approvedLeaveDates.contains(record.getDate()))
                .count();
                
        double leaveDays = approvedLeaveDates.size() + manualLeaveDays;

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

    private Set<LocalDate> getApprovedLeaveDates(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Set<LocalDate> leaveDates = new HashSet<>();
        List<LeaveRequest> approvedLeaves = leaveRequestRepository
                .findByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employeeId, LeaveRequest.LeaveStatus.APPROVED, endDate, startDate);
                        
        for (LeaveRequest leave : approvedLeaves) {
            LocalDate effectiveStart = leave.getStartDate().isBefore(startDate) ? startDate : leave.getStartDate();
            LocalDate effectiveEnd = leave.getEndDate().isAfter(endDate) ? endDate : leave.getEndDate();
            
            for (LocalDate date = effectiveStart; !date.isAfter(effectiveEnd); date = date.plusDays(1)) {
                leaveDates.add(date);
            }
        }
        return leaveDates;
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
