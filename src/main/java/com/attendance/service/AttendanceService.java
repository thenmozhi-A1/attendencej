package com.attendance.service;

import com.attendance.dto.AttendanceDTO;
import com.attendance.dto.CheckInRequest;
import com.attendance.dto.CheckOutRequest;
import com.attendance.dto.DashboardStats;
import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.Employee;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.AttendanceRecordRepository;
import com.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    private static final LocalTime LATE_THRESHOLD = LocalTime.of(9, 30);
    private static final LocalTime HALF_DAY_THRESHOLD = LocalTime.of(13, 0);

    @Transactional
    public AttendanceDTO checkIn(CheckInRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        LocalDate today = LocalDate.now();

        if (attendanceRecordRepository.existsByEmployeeIdAndDate(employee.getId(), today)) {
            throw new BadRequestException("Employee has already checked in today");
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceRecord.AttendanceStatus status = determineCheckInStatus(now.toLocalTime());

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .date(today)
                .checkInTime(now)
                .status(status)
                .remarks(request.getRemarks())
                .build();

        AttendanceRecord savedRecord = attendanceRecordRepository.save(record);
        return convertToDTO(savedRecord);
    }

    @Transactional
    public AttendanceDTO checkOut(CheckOutRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRecordRepository.findByEmployeeIdAndDate(employee.getId(), today)
                .orElseThrow(() -> new BadRequestException("No check-in record found for today"));

        if (record.getCheckOutTime() != null) {
            throw new BadRequestException("Employee has already checked out today");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setCheckOutTime(now);

        double workHours = calculateWorkHours(record.getCheckInTime(), now);
        record.setWorkHours(workHours);

        if (request.getRemarks() != null) {
            String existingRemarks = record.getRemarks() != null ? record.getRemarks() + "; " : "";
            record.setRemarks(existingRemarks + request.getRemarks());
        }

        if (workHours < 4.0 && record.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT) {
            record.setStatus(AttendanceRecord.AttendanceStatus.HALF_DAY);
        }

        AttendanceRecord updatedRecord = attendanceRecordRepository.save(record);
        return convertToDTO(updatedRecord);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getAttendanceRecords(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records;

        if (employeeId != null && startDate != null && endDate != null) {
            records = attendanceRecordRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);
        } else if (employeeId != null) {
            records = attendanceRecordRepository.findByEmployeeId(employeeId);
        } else if (startDate != null && endDate != null) {
            records = attendanceRecordRepository.findByDateBetween(startDate, endDate);
        } else {
            records = attendanceRecordRepository.findAll();
        }

        return records.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getTodayAttendance() {
        LocalDate today = LocalDate.now();
        return attendanceRecordRepository.findByDate(today).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        LocalDate today = LocalDate.now();
        long totalEmployees = employeeRepository.findByIsActiveTrue().size();
        long presentToday = attendanceRecordRepository.countDistinctEmployeeByDateAndStatus(
                today, AttendanceRecord.AttendanceStatus.PRESENT);
        long lateToday = attendanceRecordRepository.countDistinctEmployeeByDateAndStatus(
                today, AttendanceRecord.AttendanceStatus.LATE);
        long halfDayToday = attendanceRecordRepository.countDistinctEmployeeByDateAndStatus(
                today, AttendanceRecord.AttendanceStatus.HALF_DAY);
        long onLeaveToday = attendanceRecordRepository.countDistinctEmployeeByDateAndStatus(
                today, AttendanceRecord.AttendanceStatus.ON_LEAVE);
        long absentToday = totalEmployees - presentToday - lateToday - halfDayToday - onLeaveToday;
        if (absentToday < 0) absentToday = 0;

        return DashboardStats.builder()
                .totalEmployees(totalEmployees)
                .presentToday(presentToday)
                .absentToday(absentToday)
                .lateToday(lateToday)
                .onLeaveToday(onLeaveToday)
                .build();
    }

    private AttendanceRecord.AttendanceStatus determineCheckInStatus(LocalTime checkInTime) {
        if (checkInTime.isAfter(HALF_DAY_THRESHOLD)) {
            return AttendanceRecord.AttendanceStatus.HALF_DAY;
        } else if (checkInTime.isAfter(LATE_THRESHOLD)) {
            return AttendanceRecord.AttendanceStatus.LATE;
        }
        return AttendanceRecord.AttendanceStatus.PRESENT;
    }

    private double calculateWorkHours(LocalDateTime checkIn, LocalDateTime checkOut) {
        Duration duration = Duration.between(checkIn, checkOut);
        return Math.round(duration.toMinutes() / 60.0 * 100.0) / 100.0;
    }

    private AttendanceDTO convertToDTO(AttendanceRecord record) {
        return AttendanceDTO.builder()
                .id(record.getId())
                .employeeId(record.getEmployee().getId())
                .employeeName(record.getEmployee().getName())
                .employeeCode(record.getEmployee().getEmployeeCode())
                .date(record.getDate())
                .checkInTime(record.getCheckInTime())
                .checkOutTime(record.getCheckOutTime())
                .status(record.getStatus().name())
                .workHours(record.getWorkHours())
                .remarks(record.getRemarks())
                .build();
    }
}
