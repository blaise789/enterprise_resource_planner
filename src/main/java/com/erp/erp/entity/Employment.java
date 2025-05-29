package com.erp.erp.entity;

import com.erp.erp.enums.EmploymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import lombok.NoArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employments")
public class Employment {
    @Id
    private String code; // Business code, e.g., "EML001"

    @OneToOne(fetch = FetchType.LAZY) // Or ManyToOne if one employee can have multiple (historical) employments
    @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false, unique = true)
    private Employee employee;

    private String department;
    private String position;
    private BigDecimal baseSalary;
    @Enumerated(EnumType.STRING)
    private EmploymentStatus status; // ACTIVE, INACTIVE
    private LocalDate joiningDate;

    public Employment(String code, Employee employee, String department, String position, BigDecimal baseSalary, EmploymentStatus status, LocalDate joiningDate) {
        this.code = code;
        this.employee = employee;
        this.department = department;
        this.position = position;
        this.baseSalary = baseSalary;
        this.status = status;
        this.joiningDate = joiningDate;
    }
}