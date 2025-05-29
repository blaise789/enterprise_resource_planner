package com.erp.erp.dto.response;


import com.erp.erp.enums.EmploymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmploymentResponseDTO {
    private String code;
    private String employeeCode; // To identify the employee
    private String department;
    private String position;
    private BigDecimal baseSalary;
    private EmploymentStatus status;
    private LocalDate joiningDate;
}
