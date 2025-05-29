package com.erp.erp.services.Impl;


import com.erp.erp.dto.request.EmploymentRequestDTO;
import com.erp.erp.dto.response.EmploymentResponseDTO;
import com.erp.erp.entity.Employee;
import com.erp.erp.entity.Employment;
import com.erp.erp.enums.EmploymentStatus;
import com.erp.erp.exceptions.ResourceNotFoundException;
import com.erp.erp.repository.EmployeeRepository;
import com.erp.erp.repository.EmploymentRepository;
import com.erp.erp.services.EmploymentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmploymentServiceImpl implements EmploymentService {

    @Autowired
    private EmploymentRepository employmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public EmploymentResponseDTO createEmployment(EmploymentRequestDTO employmentRequestDTO) {
        Employee employee = employeeRepository.findByCode(employmentRequestDTO.getEmployeeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + employmentRequestDTO.getEmployeeCode()));

        if (employmentRepository.existsByEmployeeCode(employmentRequestDTO.getEmployeeCode())) {
            throw new IllegalArgumentException("Employment details already exist for employee: " + employmentRequestDTO.getEmployeeCode());
        }
        if (employmentRepository.existsByCode(employmentRequestDTO.getCode())) {
            throw new IllegalArgumentException("Employment code already exists: " + employmentRequestDTO.getCode());
        }


        Employment employment = new Employment();
        BeanUtils.copyProperties(employmentRequestDTO, employment);
        employment.setEmployee(employee);

        Employment savedEmployment = employmentRepository.save(employment);
        return convertToResponseDTO(savedEmployment);
    }

    @Override
    public EmploymentResponseDTO getEmploymentByEmployeeCode(String employeeCode) {
        Employment employment = employmentRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employment not found for employee code: " + employeeCode));
        return convertToResponseDTO(employment);
    }

    @Override
    @Transactional
    public EmploymentResponseDTO updateEmployment(String employeeCode, EmploymentRequestDTO employmentRequestDTO) {
        Employment employment = employmentRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employment not found for employee code: " + employeeCode));

        BeanUtils.copyProperties(employmentRequestDTO, employment, "employeeCode", "code"); // Don't update employee link or employment code from here directly

        Employment updatedEmployment = employmentRepository.save(employment);
        return convertToResponseDTO(updatedEmployment);
    }

    @Override
    public List<Employment> getActiveEmploymentsForPayroll() {
        return employmentRepository.findActiveEmploymentsForPayroll(
                com.erp.erp.enums.EmployeeStatus.ACTIVE,
                com.erp.erp.enums.EmploymentStatus.ACTIVE
        );
    }

    private EmploymentResponseDTO convertToResponseDTO(Employment employment) {
        EmploymentResponseDTO dto = new EmploymentResponseDTO();
        BeanUtils.copyProperties(employment, dto);
        dto.setEmployeeCode(employment.getEmployee().getCode());
        return dto;
    }
}
