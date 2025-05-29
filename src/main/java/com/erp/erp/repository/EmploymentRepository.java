package com.erp.erp.repository;



import com.erp.erp.entity.Employee;
import com.erp.erp.entity.Employment;
import com.erp.erp.enums.EmployeeStatus;
import com.erp.erp.enums.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, String> {
    Optional<Employment> findByEmployee(Employee employee);
    Optional<Employment> findByEmployeeCode(String employeeCode);
    Boolean existsByCode(String code);
    Boolean existsByEmployeeCode(String employeeCode);

    // To fetch active employees with their active employments
    @Query("SELECT em FROM Employment em JOIN FETCH em.employee e WHERE e.status = :employeeStatusParam AND em.status = :employmentStatusParam")
    List<Employment> findActiveEmploymentsForPayroll(
            @Param("employeeStatusParam") EmployeeStatus employeeStatus,
            @Param("employmentStatusParam") EmploymentStatus employmentStatus
    );
}
