package com.erp.erp.controllers;


import com.erp.erp.dto.request.DeductionDTO;
import com.erp.erp.entity.Deduction;
import com.erp.erp.services.DeductionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/deductions")
@Tag(name = "Deductions Management")
@SecurityRequirement(name = "bearerAuth")
public class DeductionController {

    @Autowired
    private DeductionService deductionService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @Operation(summary = "Create a new deduction type")
    public ResponseEntity<Deduction> createDeduction(@Valid @RequestBody DeductionDTO deductionDTO) {
        Deduction createdDeduction = deductionService.createDeduction(deductionDTO);
        return new ResponseEntity<>(createdDeduction, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Any authenticated user can view, or restrict further
    @Operation(summary = "Get all deductions")
    public ResponseEntity<List<Deduction>> getAllDeductions() {
        List<Deduction> deductions = deductionService.getAllDeductions();
        return ResponseEntity.ok(deductions);
    }

    @GetMapping("/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a specific deduction by code")
    public ResponseEntity<Deduction> getDeductionByCode(@PathVariable String code) {
        Deduction deduction = deductionService.getDeductionByCode(code);
        return ResponseEntity.ok(deduction);
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @Operation(summary = "Update an existing deduction")
    public ResponseEntity<Deduction> updateDeduction(@PathVariable String code, @Valid @RequestBody DeductionDTO deductionDTO) {
        Deduction updatedDeduction = deductionService.updateDeduction(code, deductionDTO);
        return ResponseEntity.ok(updatedDeduction);
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
    @Operation(summary = "Delete a deduction")
    public ResponseEntity<Void> deleteDeduction(@PathVariable String code) {
        deductionService.deleteDeduction(code);
        return ResponseEntity.noContent().build();
    }
}
