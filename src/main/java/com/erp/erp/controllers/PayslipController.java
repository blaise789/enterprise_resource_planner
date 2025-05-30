package com.erp.erp.controllers;
import com.erp.erp.dto.response.MessageResponseDTO;
import com.erp.erp.dto.response.PayslipResponseDTO;
import com.erp.erp.exceptions.ValidationException;
import com.erp.erp.security.services.UserDetailsImpl;
import com.erp.erp.services.PayRollService;
import com.erp.erp.services.PayslipPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.time.Month;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payslips")
@Tag(name = "Payroll & Payslip Management")
@SecurityRequirement(name = "bearerAuth")
public class PayslipController {

    private static final Logger logger = LoggerFactory.getLogger(PayslipController.class);

    @Autowired
    private PayRollService payrollService;

    @Autowired
    private PayslipPdfService payslipPdfService;

    /**
     * Validates month and year parameters
     * 
     * @param month the month (1-12)
     * @param year the year
     * @throws ValidationException if parameters are invalid
     */
    private void validateMonthYearParams(int month, int year) {
        if (month < 1 || month > 12) {
            throw new ValidationException("Month must be between 1 and 12");
        }

        int currentYear = java.time.LocalDate.now().getYear();
        if (year < 2000 || year > currentYear + 5) {
            throw new ValidationException("Year must be between 2000 and " + (currentYear + 5));
        }
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Generate payroll for a given month and year",
            description = "Manager starts payroll process. System computes and generates salary for all active employees.")
    public ResponseEntity<List<PayslipResponseDTO>> generatePayroll(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam int month,
            @Parameter(description = "Year (e.g., 2025)", required = true) @RequestParam int year) {
        logger.info("Request to generate payroll for {}/{}", month, year);
        validateMonthYearParams(month, year);
        List<PayslipResponseDTO> generatedPayslips = payrollService.generatePayroll(month, year);
        logger.info("Generated {} payslips for {}/{}", generatedPayslips.size(), month, year);
        return ResponseEntity.ok(generatedPayslips);
    }

    @PutMapping("/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Approve payroll for a given month and year",
            description = "Admin approves payroll, updating payslip status from PENDING to PAID. This triggers email notifications.")
    public ResponseEntity<List<PayslipResponseDTO>> approvePayroll(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam int month,
            @Parameter(description = "Year (e.g., 2025)", required = true) @RequestParam int year) {
        logger.info("Request to approve payroll for {}/{}", month, year);
        validateMonthYearParams(month, year);
        List<PayslipResponseDTO> approvedPayslips = payrollService.approvePayroll(month, year);
        logger.info("Approved {} payslips for {}/{}", approvedPayslips.size(), month, year);
        return ResponseEntity.ok(approvedPayslips);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @Operation(summary = "Get current employee's payslip for a given month and year",
            description = "Employee can view their own payslip.")
    public ResponseEntity<PayslipResponseDTO> getMyPayslip(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam int month,
            @Parameter(description = "Year (e.g., 2025)", required = true) @RequestParam int year) {
        validateMonthYearParams(month, year);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        logger.info("Employee {} requesting payslip for {}/{}", userDetails.getEmployeeCode(), month, year);
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
        logger.info("Request to get all payslips for {}/{}", month, year);
        validateMonthYearParams(month, year);
        List<PayslipResponseDTO> payslips = payrollService.getAllPayslipsForMonthYear(month, year);
        logger.info("Retrieved {} payslips for {}/{}", payslips.size(), month, year);
        return ResponseEntity.ok(payslips);
    }

    @GetMapping("/{payslipId}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get a specific payslip by its ID (for Manager/Admin)",
            description = "Allows Manager or Admin to view a specific payslip by ID.")
    public ResponseEntity<PayslipResponseDTO> getPayslipById(@PathVariable Long payslipId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/my/pdf")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @Operation(summary = "Download current employee's payslip as PDF for a given month and year",
            description = "Employee can download their own payslip as PDF.")
    public ResponseEntity<InputStreamResource> downloadMyPayslipPdf(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam int month,
            @Parameter(description = "Year (e.g., 2025)", required = true) @RequestParam int year) {
        validateMonthYearParams(month, year);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String employeeCode = userDetails.getEmployeeCode();

        logger.info("Employee {} requesting PDF payslip for {}/{}", employeeCode, month, year);

        InputStream pdfStream = payslipPdfService.generatePayslipPdf(employeeCode, month, year);

        String filename = "payslip_" + employeeCode + "_" + Month.of(month).toString() + "_" + year + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "Download a specific employee's payslip as PDF for a given month and year",
            description = "Manager or Admin can download any employee's payslip as PDF.")
    public ResponseEntity<InputStreamResource> downloadEmployeePayslipPdf(
            @Parameter(description = "Employee Code", required = true) @RequestParam String employeeCode,
            @Parameter(description = "Month (1-12)", required = true) @RequestParam int month,
            @Parameter(description = "Year (e.g., 2025)", required = true) @RequestParam int year) {
        validateMonthYearParams(month, year);

        logger.info("Request to download PDF payslip for employee {} for {}/{}", employeeCode, month, year);

        InputStream pdfStream = payslipPdfService.generatePayslipPdf(employeeCode, month, year);

        String filename = "payslip_" + employeeCode + "_" + Month.of(month).toString() + "_" + year + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }

}
