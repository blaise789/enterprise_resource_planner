package com.erp.erp.dto.request;

import java.time.LocalDate;

import com.erp.erp.enums.EmployeeStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public class AdminRequestDTO {
    
      @NotBlank(message = "Employee code cannot be blank")
    @Size(min = 3, max = 20, message = "Employee code must be between 3 and 20 characters")
    private String code;

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")

    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password; // Required for new employee creation

    @NotBlank(message = "Mobile number cannot be blank")
    private String mobile;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Status cannot be null")
    private EmployeeStatus status;
}
