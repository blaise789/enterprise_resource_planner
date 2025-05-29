package com.erp.erp.entity;
import com.erp.erp.enums.ERole;
import jakarta.persistence.*;

import jakarta.persistence.Entity;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true, nullable = false)
    private ERole name; // ROLE_MANAGER, ROLE_ADMIN, ROLE_EMPLOYEE
    // Getters, Setters

    public ERole getName() { // <--- THIS IS THE METHOD NEEDED
        return name;
    }

    public void setName(ERole name) {
        this.name = name;
    }
}