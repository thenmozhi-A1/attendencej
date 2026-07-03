package com.attendance.dto;

import com.attendance.entity.AttendanceRecord;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminAttendanceEditRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Status is required")
    private AttendanceRecord.AttendanceStatus status;

    private String remarks;
}
