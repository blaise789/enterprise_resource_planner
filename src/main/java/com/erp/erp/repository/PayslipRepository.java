package com.erp.erp.repository;


import com.erp.erp.entity.Employee;
import com.erp.erp.entity.PaySlip;
import com.erp.erp.enums.PayslipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<PaySlip, Long> {
    boolean existsByEmployeeAndMonthAndYear(Employee employee, int month, int year);
    Optional<PaySlip> findByEmployeeAndMonthAndYear(Employee employee, int month, int year);
    List<PaySlip> findByMonthAndYear(int month, int year);
    List<PaySlip> findByMonthAndYearAndStatus(int month, int year, PayslipStatus status);
}
