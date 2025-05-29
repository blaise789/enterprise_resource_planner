package com.erp.erp.dto.response;



import com.erp.erp.enums.PayslipStatus;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PayslipResponseDTO {
    private Long id;
    private String employeeCode;
    private String employeeName; // For easier display
    private BigDecimal baseSalary; // Added for clarity on payslip
    private BigDecimal houseAmount;
    private BigDecimal transportAmount;
    private BigDecimal employeeTaxedAmount;
    private BigDecimal pensionAmount;
    private BigDecimal medicalInsuranceAmount;
    private BigDecimal otherTaxedAmount;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private int month;
    private int year;
    private PayslipStatus status;
}
