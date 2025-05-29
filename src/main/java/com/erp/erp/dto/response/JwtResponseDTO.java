package com.erp.erp.dto.response;


import lombok.Data;
import java.util.List;

@Data
public class JwtResponseDTO {
    private String token;
    private String type = "Bearer";
    private String employeeCode;
    private String email;
    private List<String> roles;

    public JwtResponseDTO(String accessToken, String employeeCode, String email, List<String> roles) {
        this.token = accessToken;
        this.employeeCode = employeeCode;
        this.email = email;
        this.roles = roles;
    }
}
