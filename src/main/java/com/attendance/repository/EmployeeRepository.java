package com.attendance.repository;

import com.attendance.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    List<Employee> findByRole(Employee.Role role);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByIsActiveTrue();

    boolean existsByEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);
}
