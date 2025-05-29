package com.erp.erp.dto.request;


import com.erp.erp.enums.EmploymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmploymentRequestDTO {
    @NotBlank(message = "Employment code cannot be blank")
    private String code;

    @NotBlank(message = "Employee code is required to link employment")
    private String employeeCode; // To link to an existing employee

    @NotBlank(message = "Department cannot be blank")
    private String department;

    @NotBlank(message = "Position cannot be blank")
    private String position;

    @NotNull(message = "Base salary cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be greater than 0")
    private BigDecimal baseSalary;

    @NotNull(message = "Status cannot be null")
    private EmploymentStatus status;

    @NotNull(message = "Joining date cannot be null")
    private LocalDate joiningDate;
}
