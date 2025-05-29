package com.erp.erp.controllers;

import com.erp.erp.dto.request.EmploymentRequestDTO;
import com.erp.erp.dto.response.EmploymentResponseDTO;
import com.erp.erp.services.EmploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/employments") // Make sure this path is what you expect
@Tag(name = "employment Management")
@SecurityRequirement(name = "bearerAuth")
public class EmploymentController {

    @Autowired
    private EmploymentService employmentService; // Make sure this service exists and is a bean

    @PostMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Create employment details for an employee")
    public ResponseEntity<EmploymentResponseDTO> createEmployment(@Valid @RequestBody EmploymentRequestDTO employmentRequestDTO) {
        EmploymentResponseDTO createdEmployment = employmentService.createEmployment(employmentRequestDTO);
        return new ResponseEntity<>(createdEmployment, HttpStatus.CREATED);
    }

    // Renamed from getEmploymentByEmployeeCode to be more RESTful if employeeCode is path variable
    @GetMapping("/employee/{employeeCode}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or (hasRole('ROLE_EMPLOYEE') and #employeeCode == principal.employeeCode)")
    @Operation(summary = "Get employment details by employee code")
    public ResponseEntity<EmploymentResponseDTO> getEmploymentDetailsByEmployeeCode(@PathVariable String employeeCode) {
        EmploymentResponseDTO employment = employmentService.getEmploymentByEmployeeCode(employeeCode);
        return ResponseEntity.ok(employment);
    }

    @PutMapping("/employee/{employeeCode}")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Update employment details for an employee")
    public ResponseEntity<EmploymentResponseDTO> updateEmployment(@PathVariable String employeeCode, @Valid @RequestBody EmploymentRequestDTO employmentRequestDTO) {
        EmploymentResponseDTO updatedEmployment = employmentService.updateEmployment(employeeCode, employmentRequestDTO);
        return ResponseEntity.ok(updatedEmployment);
    }

    // You might also have a GET for a specific employment record by its own code if needed
    // @GetMapping("/{employmentCode}")
    // public ResponseEntity<EmploymentResponseDTO> getEmploymentByCode(@PathVariable String employmentCode) { ... }
}
