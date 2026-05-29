package com.attendance.service;

import com.attendance.dto.LeaveApprovalDTO;
import com.attendance.dto.LeaveRequestDTO;
import com.attendance.entity.Employee;
import com.attendance.entity.LeaveRequest;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.exception.UnauthorizedException;
import com.attendance.repository.EmployeeRepository;
import com.attendance.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveRequestDTO createLeaveRequest(LeaveRequestDTO requestDTO) {
        Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", requestDTO.getEmployeeId()));

        if (requestDTO.getStartDate().isAfter(requestDTO.getEndDate())) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }

        long daysRequested = ChronoUnit.DAYS.between(requestDTO.getStartDate(), requestDTO.getEndDate()) + 1;
        if (daysRequested > 30) {
            throw new BadRequestException("Leave request cannot exceed 30 days");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(LeaveRequest.LeaveType.valueOf(requestDTO.getLeaveType()))
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .reason(requestDTO.getReason())
                .status(LeaveRequest.LeaveStatus.PENDING)
                .build();

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        return convertToDTO(savedRequest);
    }

    @Transactional
    public LeaveRequestDTO approveOrRejectLeave(Long leaveId, LeaveApprovalDTO approvalDTO, Long approverId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request", "id", leaveId));

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", approverId));

        if (leaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new BadRequestException("Leave request is already " + leaveRequest.getStatus().name().toLowerCase());
        }

        LeaveRequest.LeaveStatus newStatus;
        try {
            newStatus = LeaveRequest.LeaveStatus.valueOf(approvalDTO.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + approvalDTO.getStatus() + ". Must be APPROVED or REJECTED");
        }

        if (newStatus != LeaveRequest.LeaveStatus.APPROVED && newStatus != LeaveRequest.LeaveStatus.REJECTED) {
            throw new BadRequestException("Can only approve or reject a leave request");
        }

        leaveRequest.setStatus(newStatus);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovalRemarks(approvalDTO.getApprovalRemarks());
        leaveRequest.setApprovedAt(LocalDateTime.now());

        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        return convertToDTO(updatedRequest);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getLeavesByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getLeavesByStatus(String status) {
        LeaveRequest.LeaveStatus leaveStatus;
        try {
            leaveStatus = LeaveRequest.LeaveStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }

        return leaveRequestRepository.findByStatus(leaveStatus).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDTO> getAllLeaves() {
        return leaveRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestDTO cancelLeaveRequest(Long leaveId, Long employeeId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request", "id", leaveId));

        if (!leaveRequest.getEmployee().getId().equals(employeeId)) {
            throw new UnauthorizedException("You can only cancel your own leave requests");
        }

        if (leaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be cancelled");
        }

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        return convertToDTO(updatedRequest);
    }

    private LeaveRequestDTO convertToDTO(LeaveRequest leaveRequest) {
        return LeaveRequestDTO.builder()
                .id(leaveRequest.getId())
                .employeeId(leaveRequest.getEmployee().getId())
                .employeeName(leaveRequest.getEmployee().getName())
                .leaveType(leaveRequest.getLeaveType().name())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus().name())
                .approvedByName(leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getName() : null)
                .approvalRemarks(leaveRequest.getApprovalRemarks())
                .build();
    }
}
