package com.erp.erp.dto.request;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductionDTO {
    private String code; // Not needed for creation if auto-generated, required for update

    @NotBlank(message = "Deduction name cannot be blank")
    private String deductionName;

    @NotNull(message = "Percentage cannot be null")
    @DecimalMin(value = "0.0", message = "Percentage must be non-negative")
    @DecimalMax(value = "1.0", message = "Percentage must be less than or equal to 1.0 (e.g., 0.30 for 30%)")
    private BigDecimal percentage;
}
