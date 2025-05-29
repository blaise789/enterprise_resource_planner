package com.erp.erp.utils;


import com.erp.erp.entity.Deduction;
import com.erp.erp.entity.Employee;
import com.erp.erp.entity.Role;
import com.erp.erp.enums.ERole;
import com.erp.erp.enums.EmployeeStatus;
import com.erp.erp.repository.DeductionRepository;
import com.erp.erp.repository.EmployeeRepository;
import com.erp.erp.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class Seeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(Seeder.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DeductionRepository deductionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional // Ensures all operations are part of a single transaction
    public void run(String... args) throws Exception {
        logger.info("Starting data initialization...");

        // Initialize Roles
        initializeRoles();

        // Initialize Deductions
        initializeDeductions();

        // Initialize a default Admin and Manager user (optional, for testing)
        initializeDefaultUsers();

        logger.info("Data initialization finished.");
    }

    private void initializeRoles() {
        Arrays.stream(ERole.values()).forEach(roleEnum -> {
            if (roleRepository.findByName(roleEnum).isEmpty()) {
                Role role = new Role();
                role.setName(roleEnum);
                roleRepository.save(role);
                logger.info("Created role: {}", roleEnum.name());
            }
        });
    }

    private void initializeDeductions() {
        // Code, Name, Percentage
        Object[][] defaultDeductions = {
                {"DED_TAX", "Employee Tax", new BigDecimal("0.30")},
                {"DED_PENSION", "Pension", new BigDecimal("0.06")}, // New rate
                {"DED_MED", "Medical Insurance", new BigDecimal("0.05")},
                {"DED_OTHERS", "Others", new BigDecimal("0.05")},
                {"DED_HOUSING", "Housing", new BigDecimal("0.14")},
                {"DED_TRANSPORT", "Transport", new BigDecimal("0.14")}
        };

        for (Object[] dedData : defaultDeductions) {
            String code = (String) dedData[0];
            String name = (String) dedData[1];
            BigDecimal percentage = (BigDecimal) dedData[2];

            if (deductionRepository.findByDeductionName(name).isEmpty()) {
                Deduction deduction = new Deduction();
                deduction.setCode(code); // Use predefined code
                deduction.setDeductionName(name);
                deduction.setPercentage(percentage);
                deductionRepository.save(deduction);
                logger.info("Created deduction: {} ({}) with {}%", name, code, percentage.multiply(BigDecimal.valueOf(100)));
            }
        }
    }

    private void initializeDefaultUsers() {
        // Admin User
        if (employeeRepository.findByEmail("admin@rwerp.gov").isEmpty()) {
            Employee admin = new Employee();
            admin.setCode("ADM001");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@rwerp.gov");
            admin.setPassword(passwordEncoder.encode("AdminP@ssw0rd1"));
            admin.setMobile("0788000001");
            admin.setDateOfBirth(LocalDate.of(1980, 1, 1));
            admin.setStatus(EmployeeStatus.ACTIVE);
            Set<Role> adminRoles = new HashSet<>();
            roleRepository.findByName(ERole.ROLE_ADMIN).ifPresent(adminRoles::add);
            roleRepository.findByName(ERole.ROLE_MANAGER).ifPresent(adminRoles::add); // Admin might also have manager capabilities
            admin.setRoles(adminRoles);
            employeeRepository.save(admin);
            logger.info("Created default admin user: admin@rwerp.gov");
        }

        // Manager User
        if (employeeRepository.findByEmail("manager@rwerp.gov").isEmpty()) {
            Employee manager = new Employee();
            manager.setCode("MGR001");
            manager.setFirstName("Manager");
            manager.setLastName("UserOne");
            manager.setEmail("manager@rwerp.gov");
            manager.setPassword(passwordEncoder.encode("ManagerP@ss1"));
            manager.setMobile("0788000002");
            manager.setDateOfBirth(LocalDate.of(1985, 5, 5));
            manager.setStatus(EmployeeStatus.ACTIVE);
            Set<Role> managerRoles = new HashSet<>();
            roleRepository.findByName(ERole.ROLE_MANAGER).ifPresent(managerRoles::add);
            manager.setRoles(managerRoles);
            employeeRepository.save(manager);
            logger.info("Created default manager user: manager@rwerp.gov");
        }

        // Employee User
        if (employeeRepository.findByEmail("employee@rwerp.gov").isEmpty()) {
            Employee employee = new Employee();
            employee.setCode("EMP001");
            employee.setFirstName("Employee");
            employee.setLastName("UserTest");
            employee.setEmail("employee@rwerp.gov");
            employee.setPassword(passwordEncoder.encode("EmployeeP@ss1"));
            employee.setMobile("0788000003");
            employee.setDateOfBirth(LocalDate.of(1990, 10, 10));
            employee.setStatus(EmployeeStatus.ACTIVE);
            Set<Role> employeeRoles = new HashSet<>();
            roleRepository.findByName(ERole.ROLE_EMPLOYEE).ifPresent(employeeRoles::add);
            employee.setRoles(employeeRoles);
            employeeRepository.save(employee);
            logger.info("Created default employee user: employee@rwerp.gov");
        }
    }
}
