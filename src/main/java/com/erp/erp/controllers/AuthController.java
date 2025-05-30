package com.erp.erp.controllers;

import com.erp.erp.dto.request.EmployeeRequestDTO;
import com.erp.erp.dto.request.LoginRequest;
import com.erp.erp.dto.response.EmployeeResponseDTO;
import com.erp.erp.dto.response.JwtResponseDTO;
import com.erp.erp.dto.response.MessageResponseDTO;
import com.erp.erp.entity.Role;
import com.erp.erp.enums.ERole;
import com.erp.erp.repository.RoleRepository;
import com.erp.erp.security.jwt.JwtUtils;
import com.erp.erp.security.services.UserDetailsImpl;
import com.erp.erp.services.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API for user authentication and admin management")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RoleRepository roleRepository;

    @Operation(
        summary = "Authenticate user and return JWT token",
        description = "Employees authenticate using email and password.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Authentication successful", 
                content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Parameter(description = "Login credentials", required = true) 
            @Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponseDTO(jwt,
                userDetails.getEmployeeCode(),
                userDetails.getUsername(), // This is the email
                roles));
    }

    @Operation(
        summary = "Create a new admin user",
        description = "Creates a new user with admin privileges. Only existing admins can create new admin users.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "201", description = "Admin user created successfully", 
                content = @Content(schema = @Schema(implementation = EmployeeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not an admin")
        }
    )
    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<EmployeeResponseDTO> createAdminUser(
            @Parameter(description = "Admin user details", required = true) 
            @Valid @RequestBody EmployeeRequestDTO employeeRequestDTO) {

        // Ensure the user has admin role
        Set<String> roles = new HashSet<>();
        roles.add(ERole.ROLE_ADMIN.name());

        // Add ROLE_MANAGER as well since admins typically have manager capabilities
        roles.add(ERole.ROLE_MANAGER.name());

        // Override any roles provided in the request
        employeeRequestDTO.setRoles(roles);

        // Create the admin user
        EmployeeResponseDTO createdAdmin = employeeService.createEmployee(employeeRequestDTO);

        return new ResponseEntity<>(createdAdmin, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Logout user",
        description = "Client-side should discard the JWT. This endpoint clears the security context.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Logout successful", 
                content = @Content(schema = @Schema(implementation = MessageResponseDTO.class)))
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        MessageResponseDTO response = new MessageResponseDTO();
        response.setMessageContent("Logout successful!");
        return ResponseEntity.ok(response);
    }
}
