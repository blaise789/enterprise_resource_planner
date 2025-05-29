package com.erp.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private String employeeCode;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
}
