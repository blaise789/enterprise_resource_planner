package com.erp.erp.controllers;
import com.erp.erp.dto.response.MessageResponseDTO;
import com.erp.erp.dto.response.PayslipResponseDTO;
import com.erp.erp.security.services.UserDetailsImpl;
import com.erp.erp.services.PayRollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/payslips")
@Tag(name = "Payroll & Payslip Management")
@SecurityRequirement(name = "bearerAuth")
public class PayslipController {

    @Autowired
    private PayRollService payrollService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Generate payroll for a given month and year",
            description = "Manager starts payroll process. System computes and generates salary for all active employees.")
    public ResponseEntity<List<PayslipResponseDTO>> generatePayroll(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam int month,
            @Parameter(description = "Year (e.g., 2025)", required = true) @RequestParam int year) {
        List<PayslipResponseDTO> generatedPayslips = payrollService.generatePayroll(month, year);
        return ResponseEntity.ok(generatedPayslips);
    }

    @PutMapping("/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Approve payroll for a given month and year",
            description = "Admin approves payroll, updating payslip status from PENDING to PAID. This triggers email notifications.")
    public ResponseEntity<List<PayslipResponseDTO>> approvePayroll(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam int month,
            @Parameter(description = "Year (e.g., 2025)", required = true) @RequestParam int year) {
        List<PayslipResponseDTO> approvedPayslips = payrollService.approvePayroll(month, year);
        if (approvedPayslips.isEmpty()) {
            return ResponseEntity.ok(approvedPayslips);
        }
        return ResponseEntity.ok(approvedPayslips);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @Operation(summary = "Get current employee's payslip for a given month and year",
            description = "Employee can view their own payslip.")
    public ResponseEntity<PayslipResponseDTO> getMyPayslip(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam int month,
            @Parameter(description = "Year (e.g., 2025)", required = true) @RequestParam int year) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        PayslipResponseDTO payslip = payrollService.getPayslipForEmployee(userDetails.getEmployeeCode(), month, year);
        return ResponseEntity.ok(payslip);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Get all payslips for a given month and year",
            description = "Manager can view all payslips for a given period.")
    public ResponseEntity<List<PayslipResponseDTO>> getAllPayslips(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam int month,
            @Parameter(description = "Year (e.g., 2025)", required = true) @RequestParam int year) {
        List<PayslipResponseDTO> payslips = payrollService.getAllPayslipsForMonthYear(month, year);
        return ResponseEntity.ok(payslips);
    }

    @GetMapping("/{payslipId}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get a specific payslip by its ID (for Manager/Admin)",
            description = "Allows Manager or Admin to view a specific payslip by ID.")
    public ResponseEntity<PayslipResponseDTO> getPayslipById(@PathVariable Long payslipId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

}