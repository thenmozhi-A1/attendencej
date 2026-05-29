package com.attendance.service;

import com.attendance.dto.EmployeeDTO;
import com.attendance.entity.Department;
import com.attendance.entity.Employee;
import com.attendance.exception.BadRequestException;
import com.attendance.exception.ResourceNotFoundException;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getActiveEmployees() {
        return employeeRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return convertToDTO(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByRole(Employee.Role role) {
        return employeeRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new BadRequestException("Employee with email " + employeeDTO.getEmail() + " already exists");
        }

        if (employeeRepository.existsByEmployeeCode(employeeDTO.getEmployeeCode())) {
            throw new BadRequestException("Employee with code " + employeeDTO.getEmployeeCode() + " already exists");
        }

        Employee employee = Employee.builder()
                .employeeCode(employeeDTO.getEmployeeCode())
                .name(resolveName(employeeDTO))
                .email(employeeDTO.getEmail())
                .phone(employeeDTO.getPhone())
                .role(Employee.Role.valueOf(employeeDTO.getRole().toUpperCase()))
                .monthlySalary(employeeDTO.getMonthlySalary() != null ? employeeDTO.getMonthlySalary() : BigDecimal.ZERO)
                .isActive(employeeDTO.getIsActive() != null ? employeeDTO.getIsActive() : true)
                .build();

        if (employeeDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", employeeDTO.getDepartmentId()));
            employee.setDepartment(department);
        }

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (employeeDTO.getName() != null) {
            employee.setName(employeeDTO.getName());
        } else if (employeeDTO.getFirstName() != null || employeeDTO.getLastName() != null) {
            employee.setName(resolveName(employeeDTO));
        }
        if (employeeDTO.getEmail() != null && !employeeDTO.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
                throw new BadRequestException("Employee with email " + employeeDTO.getEmail() + " already exists");
            }
            employee.setEmail(employeeDTO.getEmail());
        }
        if (employeeDTO.getPhone() != null) {
            employee.setPhone(employeeDTO.getPhone());
        }
        if (employeeDTO.getRole() != null) {
            employee.setRole(Employee.Role.valueOf(employeeDTO.getRole().toUpperCase()));
        }
        if (employeeDTO.getStatus() != null) {
            employee.setIsActive(!"inactive".equalsIgnoreCase(employeeDTO.getStatus()));
        }
        if (employeeDTO.getIsActive() != null) {
            employee.setIsActive(employeeDTO.getIsActive());
        }
        if (employeeDTO.getMonthlySalary() != null) {
            employee.setMonthlySalary(employeeDTO.getMonthlySalary());
        }
        if (employeeDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", employeeDTO.getDepartmentId()));
            employee.setDepartment(department);
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        employee.setIsActive(false);
        employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public long getTotalActiveEmployees() {
        return employeeRepository.findByIsActiveTrue().size();
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        String[] nameParts = splitName(employee.getName());
        return EmployeeDTO.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .name(employee.getName())
                .firstName(nameParts[0])
                .lastName(nameParts[1])
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .role(employee.getRole().name().toLowerCase())
                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .department(employee.getDepartment() != null ? employee.getDepartment().getName() : null)
                .monthlySalary(employee.getMonthlySalary())
                .isActive(employee.getIsActive())
                .status(Boolean.TRUE.equals(employee.getIsActive()) ? "active" : "inactive")
                .createdAt(employee.getCreatedAt())
                .build();
    }

    private String resolveName(EmployeeDTO employeeDTO) {
        if (employeeDTO.getName() != null && !employeeDTO.getName().isBlank()) {
            return employeeDTO.getName().trim();
        }

        String firstName = employeeDTO.getFirstName() != null ? employeeDTO.getFirstName().trim() : "";
        String lastName = employeeDTO.getLastName() != null ? employeeDTO.getLastName().trim() : "";
        return (firstName + " " + lastName).trim();
    }

    private String[] splitName(String name) {
        if (name == null || name.isBlank()) {
            return new String[]{"", ""};
        }
        String[] parts = name.trim().split("\\s+", 2);
        return new String[]{parts[0], parts.length > 1 ? parts[1] : ""};
    }
}
