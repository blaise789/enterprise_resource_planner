package com.erp.erp.entity;

import com.erp.erp.enums.PayslipStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payslips", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_code", "month", "year"})
})
public class PaySlip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false)
    private Employee employee;

    private BigDecimal houseAmount;
    private BigDecimal transportAmount;
    private BigDecimal employeeTaxedAmount;
    private BigDecimal pensionAmount;
    private BigDecimal medicalInsuranceAmount;
    private BigDecimal otherTaxedAmount;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;

    @Column(nullable = false)
    private int month; // 1-12
    @Column(nullable = false)
    private int year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayslipStatus status; // PENDING, PAID
    // Getters, Setters

    public PaySlip() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BigDecimal getHouseAmount() {
        return houseAmount;
    }

    public void setHouseAmount(BigDecimal houseAmount) {
        this.houseAmount = houseAmount;
    }

    public BigDecimal getTransportAmount() {
        return transportAmount;
    }

    public void setTransportAmount(BigDecimal transportAmount) {
        this.transportAmount = transportAmount;
    }

    public BigDecimal getEmployeeTaxedAmount() {
        return employeeTaxedAmount;
    }

    public void setEmployeeTaxedAmount(BigDecimal employeeTaxedAmount) {
        this.employeeTaxedAmount = employeeTaxedAmount;
    }

    public BigDecimal getPensionAmount() {
        return pensionAmount;
    }

    public void setPensionAmount(BigDecimal pensionAmount) {
        this.pensionAmount = pensionAmount;
    }

    public BigDecimal getMedicalInsuranceAmount() {
        return medicalInsuranceAmount;
    }

    public void setMedicalInsuranceAmount(BigDecimal medicalInsuranceAmount) {
        this.medicalInsuranceAmount = medicalInsuranceAmount;
    }

    public BigDecimal getOtherTaxedAmount() {
        return otherTaxedAmount;
    }

    public void setOtherTaxedAmount(BigDecimal otherTaxedAmount) {
        this.otherTaxedAmount = otherTaxedAmount;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public PayslipStatus getStatus() {
        return status;
    }

    public void setStatus(PayslipStatus status) {
        this.status = status;
    }

    // Optional: You might also want an all-arguments constructor if you use it elsewhere
    public PaySlip(Employee employee, BigDecimal houseAmount, BigDecimal transportAmount,
                   BigDecimal employeeTaxedAmount, BigDecimal pensionAmount, BigDecimal medicalInsuranceAmount,
                   BigDecimal otherTaxedAmount, BigDecimal grossSalary, BigDecimal netSalary,
                   int month, int year, PayslipStatus status) {
        this.employee = employee;
        this.houseAmount = houseAmount;
        this.transportAmount = transportAmount;
        this.employeeTaxedAmount = employeeTaxedAmount;
        this.pensionAmount = pensionAmount;
        this.medicalInsuranceAmount = medicalInsuranceAmount;
        this.otherTaxedAmount = otherTaxedAmount;
        this.grossSalary = grossSalary;
        this.netSalary = netSalary;
        this.month = month;
        this.year = year;
        this.status = status;
    }
}