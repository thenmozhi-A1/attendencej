package com.attendance.repository;

import com.attendance.entity.RegularizationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegularizationRequestRepository extends JpaRepository<RegularizationRequest, Long> {
    List<RegularizationRequest> findByStatus(RegularizationRequest.RequestStatus status);
}
