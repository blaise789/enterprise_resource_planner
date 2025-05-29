package com.erp.erp.services;



import com.erp.erp.dto.request.EmploymentRequestDTO;
import com.erp.erp.dto.response.EmploymentResponseDTO;
import com.erp.erp.entity.Employment;
import com.erp.erp.enums.EmploymentStatus;

import java.util.List;

public interface EmploymentService {
    EmploymentResponseDTO createEmployment(EmploymentRequestDTO employmentRequestDTO);
    EmploymentResponseDTO getEmploymentByEmployeeCode(String employeeCode);
    EmploymentResponseDTO updateEmployment(String employeeCode, EmploymentRequestDTO employmentRequestDTO);
    List<Employment> getActiveEmploymentsForPayroll();
}