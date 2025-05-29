package com.erp.erp.repository;


import com.erp.erp.entity.Deduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DeductionRepository extends JpaRepository<Deduction, String> {
    Optional<Deduction> findByDeductionName(String deductionName);
    boolean existsByDeductionName(String deductionName);
    boolean existsByCode(String code);
}
