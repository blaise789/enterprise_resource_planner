package com.erp.erp.services.Impl;

import com.erp.erp.dto.request.DeductionDTO;
import com.erp.erp.entity.Deduction;
import com.erp.erp.exceptions.ResourceNotFoundException;
import com.erp.erp.repository.DeductionRepository;
import com.erp.erp.services.DeductionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DeductionServiceImpl implements DeductionService {

    @Autowired
    private DeductionRepository deductionRepository;

    @Override
    @Transactional
    public Deduction createDeduction(DeductionDTO deductionDTO) {
        if (deductionRepository.existsByDeductionName(deductionDTO.getDeductionName())) {
            throw new IllegalArgumentException("Deduction with name '" + deductionDTO.getDeductionName() + "' already exists.");
        }
        Deduction deduction = new Deduction();
        BeanUtils.copyProperties(deductionDTO, deduction);
        if (deduction.getCode() == null || deduction.getCode().isBlank()) {
            // Generate a simple code if not provided
            String baseCode = deductionDTO.getDeductionName().replaceAll("\\s+", "_").toUpperCase();
            String uniqueCode = baseCode.substring(0, Math.min(baseCode.length(), 7)) + "_" + UUID.randomUUID().toString().substring(0, 4);
            deduction.setCode(uniqueCode);
        } else if (deductionRepository.existsByCode(deductionDTO.getCode())) {
            throw new IllegalArgumentException("Deduction with code '" + deductionDTO.getCode() + "' already exists.");
        }


        return deductionRepository.save(deduction);
    }

    @Override
    public Deduction getDeductionByCode(String code) {
        return deductionRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found with code: " + code));
    }

    @Override
    public Deduction getDeductionByName(String name) {
        return deductionRepository.findByDeductionName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found with name: " + name));
    }

    @Override
    public List<Deduction> getAllDeductions() {
        return deductionRepository.findAll();
    }

    @Override
    @Transactional
    public Deduction updateDeduction(String code, DeductionDTO deductionDTO) {
        Deduction deduction = deductionRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction not found with code: " + code));

        // Check if name is being changed and if the new name already exists for another deduction
        if (!deduction.getDeductionName().equals(deductionDTO.getDeductionName()) &&
                deductionRepository.existsByDeductionName(deductionDTO.getDeductionName())) {
            throw new IllegalArgumentException("Another deduction with name '" + deductionDTO.getDeductionName() + "' already exists.");
        }

        deduction.setDeductionName(deductionDTO.getDeductionName());
        deduction.setPercentage(deductionDTO.getPercentage());
        return deductionRepository.save(deduction);
    }

    @Override
    @Transactional
    public void deleteDeduction(String code) {
        if (!deductionRepository.existsById(code)) {
            throw new ResourceNotFoundException("Deduction not found with code: " + code);
        }
        deductionRepository.deleteById(code);
    }
}
