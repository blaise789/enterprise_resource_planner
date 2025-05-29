package com.erp.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;     // Option 1: Use Lombok
import lombok.Setter;    // Option 1: Use Lombok
import lombok.NoArgsConstructor; // Option 1: Use Lombok
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter             // Lombok
@Setter
@Entity
@Table(name = "deductions")
public class Deduction {
    @Id
    private String code; // E.g., "DED_TAX", "DED_PENSION"
    @Column(unique = true, nullable = false)
    private String deductionName;
    @Column(nullable = false)
    private BigDecimal percentage; // Store as 0.30 for 30%
    // Getters, Setters

    public Deduction() {} // No-arg constructor

    public Deduction(String code, String deductionName, BigDecimal percentage) {
        this.code = code;
        this.deductionName = deductionName;
        this.percentage = percentage;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDeductionName() { return deductionName; } // NEEDED
    public void setDeductionName(String deductionName) { this.deductionName = deductionName; }

    public BigDecimal getPercentage() { return percentage; } // NEEDED
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
}
