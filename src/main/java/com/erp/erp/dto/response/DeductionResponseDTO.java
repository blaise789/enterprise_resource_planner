package com.erp.erp.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductionResponseDTO {
    private String code;
    private String deductionName;
    private BigDecimal percentage;
}
