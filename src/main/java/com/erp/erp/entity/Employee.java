package com.erp.erp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.erp.erp.enums.EmployeeStatus;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    private String code; // Business code, e.g., "EMP001"
    private String firstName;
    private String lastName;
    @Column(unique = true, nullable = false)
    private String email;
    @JsonIgnore // Don't send password in responses
    private String password;
    private String mobile;
    private LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status; // ACTIVE, DISABLED

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "employee_roles",
            joinColumns = @JoinColumn(name = "employee_code"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    public Employee(String code, String firstName, String lastName, String email, String password, String mobile, LocalDate dateOfBirth, EmployeeStatus status) {
        this.code = code;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.mobile = mobile;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
    }

    public Employee() {

    }


    public Set<Role> getRoles() { return roles; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getCode() { return code; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMobile() { return mobile; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public EmployeeStatus getStatus() { return status; }

    // Setters
    public void setCode(String code) { this.code = code; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { // <--- ENSURE THIS METHOD EXISTS
        this.password = password;
    }

    public void setMobile(String mobile) { this.mobile = mobile; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setStatus(EmployeeStatus status) { this.status = status; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }


    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }
}

