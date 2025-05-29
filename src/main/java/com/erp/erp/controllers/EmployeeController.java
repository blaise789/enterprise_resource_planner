package com.erp.erp.controllers;


import com.erp.erp.dto.request.EmployeeRequestDTO;
import com.erp.erp.dto.response.EmployeeResponseDTO;
import com.erp.erp.security.services.UserDetailsImpl;
import com.erp.erp.services.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Create a new employee", description = "Only users with ROLE_MANAGER can create new employees.")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO employeeRequestDTO) {
        EmployeeResponseDTO createdEmployee = employeeService.createEmployee(employeeRequestDTO);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @GetMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or (hasRole('ROLE_EMPLOYEE') and #code == principal.employeeCode)")
    @Operation(summary = "Get employee details by code", description = "Managers can get any employee. Employees can only get their own details.")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeByCode(@PathVariable String code) {
        EmployeeResponseDTO employee = employeeService.getEmployeeByCode(code);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/my-details")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get current authenticated employee's details")
    public ResponseEntity<EmployeeResponseDTO> getCurrentEmployeeDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        EmployeeResponseDTO employee = employeeService.getEmployeeByCode(userDetails.getEmployeeCode());
        return ResponseEntity.ok(employee);
    }


    @GetMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Get all employees", description = "Only users with ROLE_MANAGER can view all employees.")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Update employee details", description = "Only users with ROLE_MANAGER can update employees.")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(@PathVariable String code, @Valid @RequestBody EmployeeRequestDTO employeeRequestDTO) {
        EmployeeResponseDTO updatedEmployee = employeeService.updateEmployee(code, employeeRequestDTO);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Disable an employee (soft delete)", description = "Only users with ROLE_MANAGER can disable employees.")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String code) {
        employeeService.deleteEmployee(code);
        return ResponseEntity.noContent().build();
    }
}