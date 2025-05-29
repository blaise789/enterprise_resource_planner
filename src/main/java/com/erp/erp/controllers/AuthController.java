package com.erp.erp.controllers;

import com.erp.erp.dto.request.LoginRequest;
import com.erp.erp.dto.response.JwtResponseDTO;
import com.erp.erp.dto.response.MessageResponseDTO; // Assuming you have a simple message DTO
import com.erp.erp.security.jwt.JwtUtils;
import com.erp.erp.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "api for user authentication")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Operation(summary = "Authenticate user and return JWT token",
            description = "Employees authenticate using email and password.")
    @ApiResponse(responseCode = "200", description = "Authentication successful, JWT returned")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

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

    // Optional: Endpoint for user registration if self-registration is allowed.
    // The current project description implies employees are added by managers.
    // If you need a registration endpoint (e.g., for initial admin setup), it would go here.
    // For now, employee creation is handled via EmployeeController by ROLE_MANAGER.

    @Operation(summary = "Logout user (Placeholder)",
            description = "Client-side should discard the JWT. This endpoint is a placeholder for potential server-side session invalidation if needed (e.g., token blacklisting).")
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        MessageResponseDTO response = new MessageResponseDTO(); // Uses @NoArgsConstructor
        response.setMessageContent("Logout successful!");
        // All other fields will be null or their default (0 for int)
        return ResponseEntity.ok(response);
    }
}
