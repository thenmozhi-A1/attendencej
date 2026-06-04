package com.attendance.service;

import com.attendance.dto.EmployeeDTO;
import com.attendance.exception.BadRequestException;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void createEmployee_whenRoleIsTechAndFingerprintMissing_shouldThrowBadRequestException() {
        EmployeeDTO employeeDTO = EmployeeDTO.builder()
                .employeeCode("EMP001")
                .firstName("Alice")
                .lastName("Tech")
                .email("alice.tech@example.com")
                .role("tech")
                .monthlySalary(BigDecimal.valueOf(5000))
                .isActive(true)
                .build();

        when(employeeRepository.existsByEmail(employeeDTO.getEmail())).thenReturn(false);
        when(employeeRepository.existsByEmployeeCode(employeeDTO.getEmployeeCode())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> employeeService.createEmployee(employeeDTO));
    }
}
