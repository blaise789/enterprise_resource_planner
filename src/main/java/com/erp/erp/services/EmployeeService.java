package com.erp.erp.services;



import com.erp.erp.dto.request.EmployeeRequestDTO;
import com.erp.erp.dto.response.EmployeeResponseDTO;
import java.util.List;

public interface EmployeeService {
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO employeeRequestDTO);
    EmployeeResponseDTO getEmployeeByCode(String code);
    List<EmployeeResponseDTO> getAllEmployees();
    EmployeeResponseDTO updateEmployee(String code, EmployeeRequestDTO employeeRequestDTO);
    void deleteEmployee(String code); // Soft delete
    EmployeeResponseDTO getEmployeeByEmail(String email);
}