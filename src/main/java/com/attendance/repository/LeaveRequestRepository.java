package com.attendance.repository;

import com.attendance.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveRequest.LeaveStatus status);

    List<LeaveRequest> findAllByOrderByCreatedAtDesc();

    List<LeaveRequest> findByEmployeeIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId,
            LeaveRequest.LeaveStatus status,
            LocalDate endDate,
            LocalDate startDate);
}
