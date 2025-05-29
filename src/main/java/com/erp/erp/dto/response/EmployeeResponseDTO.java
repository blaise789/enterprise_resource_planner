package com.erp.erp.dto.response;


import com.erp.erp.enums.EmployeeStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.Set;

@Data
public class EmployeeResponseDTO {
    private String code;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private LocalDate dateOfBirth;
    private EmployeeStatus status;
    private Set<String> roles;
}