package com.attendance.config;

import com.attendance.entity.Department;
import com.attendance.entity.Employee;
import com.attendance.repository.DepartmentRepository;
import com.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (employeeRepository.count() == 0) {
            // Seed a default department
            Department hrDept = Department.builder()
                    .name("Human Resources")
                    .description("HR Department")
                    .build();
            departmentRepository.save(hrDept);

            Department techDept = Department.builder()
                    .name("Engineering")
                    .description("Engineering Department")
                    .build();
            departmentRepository.save(techDept);

            // Seed a default admin employee
            Employee adminEmployee = Employee.builder()
                    .employeeCode("EMP001")
                    .name("System Admin")
                    .email("admin@attendance.com")
                    .phone("1234567890")
                    .role(Employee.Role.ADMIN)
                    .department(hrDept)
                    .monthlySalary(new BigDecimal("150000.00"))
                    .isActive(true)
                    .password(passwordEncoder.encode("admin"))
                    .build();
            employeeRepository.save(adminEmployee);

            // Seed a tech employee
            Employee techEmployee = Employee.builder()
                    .employeeCode("EMP002")
                    .name("Tech User")
                    .email("tech@attendance.com")
                    .phone("0987654321")
                    .role(Employee.Role.TECH)
                    .department(techDept)
                    .monthlySalary(new BigDecimal("120000.00"))
                    .isActive(true)
                    .password(passwordEncoder.encode("password123"))
                    .build();
            employeeRepository.save(techEmployee);

            System.out.println("=================================================");
            System.out.println("DATABASE SEEDED SUCCESSFULLY WITH DEFAULT USERS!");
            System.out.println("Admin login -> Username: admin@attendance.com (or EMP001), Password: admin");
            System.out.println("=================================================");
        }
    }
}
