package com.attendance.repository;

import com.attendance.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByEmployeeId(Long employeeId);

    List<AttendanceRecord> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    Optional<AttendanceRecord> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<AttendanceRecord> findByDate(LocalDate date);

    List<AttendanceRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);

    long countByDateAndStatus(LocalDate date, AttendanceRecord.AttendanceStatus status);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.employee.id = :employeeId AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date DESC")
    List<AttendanceRecord> findByEmployeeIdAndDateRangeOrderByDateDesc(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT a.employee.id) FROM AttendanceRecord a WHERE a.date = :date AND a.status = :status")
    long countDistinctEmployeeByDateAndStatus(@Param("date") LocalDate date, @Param("status") AttendanceRecord.AttendanceStatus status);

    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);
}
