package com.erp.erp.services;


import com.erp.erp.dto.request.DeductionDTO;
import com.erp.erp.entity.Deduction;
import java.util.List;

public interface DeductionService {
    Deduction createDeduction(DeductionDTO deductionDTO);
    Deduction getDeductionByCode(String code);
    Deduction getDeductionByName(String name);
    List<Deduction> getAllDeductions();
    Deduction updateDeduction(String code, DeductionDTO deductionDTO);
    void deleteDeduction(String code);
}
