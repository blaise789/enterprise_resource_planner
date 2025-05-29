package com.erp.erp.repository;



import com.erp.erp.entity.Employee;
import com.erp.erp.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByCode(String code);
    Optional<Employee> findByCode(String code);

    @Query("SELECT e FROM Employee e JOIN FETCH e.roles WHERE e.status = :status")
    List<Employee> findAllByStatusWithRoles(EmployeeStatus status);
}