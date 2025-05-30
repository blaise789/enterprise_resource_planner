package com.erp.erp.repository;

import com.erp.erp.entity.Employee;
import com.erp.erp.entity.Message;
import com.erp.erp.enums.MessageSentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByMonthAndYearAndEmailSentStatus(int month, int year, MessageSentStatus status);

    @Query("SELECT m FROM Message m WHERE m.employee.code = :employeeCode AND m.month = :month AND m.year = :year AND m.emailSentStatus = :status")
    List<Message> findByEmployeeCodeAndMonthAndYearAndEmailSentStatus(
            @Param("employeeCode") String employeeCode, 
            @Param("month") int month, 
            @Param("year") int year, 
            @Param("status") MessageSentStatus status);
}
