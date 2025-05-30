package com.erp.erp.services.Impl;


import com.erp.erp.dto.request.EmployeeRequestDTO;
import com.erp.erp.dto.response.EmployeeResponseDTO;
import com.erp.erp.entity.Employee;
import com.erp.erp.entity.Role;
import com.erp.erp.enums.ERole;
import com.erp.erp.enums.EmployeeStatus;
import com.erp.erp.exceptions.ResourceNotFoundException;
import com.erp.erp.exceptions.ValidationException;
import com.erp.erp.repository.EmployeeRepository;
import com.erp.erp.repository.RoleRepository;
import com.erp.erp.services.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO employeeRequestDTO) {
        if (employeeRepository.existsByCode(employeeRequestDTO.getCode())) {
            throw new IllegalArgumentException("Error: Employee code is already taken!");
        }
        if (employeeRepository.existsByEmail(employeeRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeRequestDTO, employee, "password", "roles");
        employee.setPassword(passwordEncoder.encode(employeeRequestDTO.getPassword()));

        Set<Role> roles = new HashSet<>();
        if (employeeRequestDTO.getRoles() == null || employeeRequestDTO.getRoles().isEmpty()) {
            Role userRole = roleRepository.findByName(ERole.ROLE_EMPLOYEE)
                    .orElseThrow(() -> new RuntimeException("Error: Role_Employee is not found. Initialize roles."));
            roles.add(userRole);
        } else {
            employeeRequestDTO.getRoles().forEach(roleStr -> {
                try {
                    ERole roleEnum = ERole.valueOf(roleStr);
                    Role role = roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new ValidationException("Error: Role " + roleStr + " is not found in the database."));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Error: Invalid role '" + roleStr + "'. Valid roles are: " + 
                            String.join(", ", Arrays.stream(ERole.values()).map(Enum::name).toArray(String[]::new)));
                }
            });
        }
        employee.setRoles(roles);

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToResponseDTO(savedEmployee);
    }

    @Override
    public EmployeeResponseDTO getEmployeeByCode(String code) {
        Employee employee = employeeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + code));
        return convertToResponseDTO(employee);
    }
    @Override
    public EmployeeResponseDTO getEmployeeByEmail(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with email: " + email));
        return convertToResponseDTO(employee);
    }


    @Override
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(String code, EmployeeRequestDTO employeeRequestDTO) {
        Employee employee = employeeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + code));

        // Check if email is being changed and if the new email is already taken by another user
        if (!employee.getEmail().equals(employeeRequestDTO.getEmail()) && employeeRepository.existsByEmail(employeeRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use by another employee!");
        }

        BeanUtils.copyProperties(employeeRequestDTO, employee, "code", "password", "roles"); // code and password usually not updated here

        if (employeeRequestDTO.getPassword() != null && !employeeRequestDTO.getPassword().isEmpty()) {
            employee.setPassword(passwordEncoder.encode(employeeRequestDTO.getPassword()));
        }

        if (employeeRequestDTO.getRoles() != null && !employeeRequestDTO.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            employeeRequestDTO.getRoles().forEach(roleStr -> {
                try {
                    ERole roleEnum = ERole.valueOf(roleStr);
                    Role role = roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new ValidationException("Error: Role " + roleStr + " is not found in the database."));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new ValidationException("Error: Invalid role '" + roleStr + "'. Valid roles are: " + 
                            String.join(", ", Arrays.stream(ERole.values()).map(Enum::name).toArray(String[]::new)));
                }
            });
            employee.setRoles(roles);
        }


        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToResponseDTO(updatedEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(String code) { // Soft delete
        Employee employee = employeeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + code));
        employee.setStatus(EmployeeStatus.DISABLED);
        employeeRepository.save(employee);
    }

    private EmployeeResponseDTO convertToResponseDTO(Employee employee) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        BeanUtils.copyProperties(employee, dto, "roles");
        dto.setRoles(employee.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet()));
        return dto;
    }
}
