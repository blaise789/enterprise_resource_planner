package com.erp.erp.repository;


import com.erp.erp.entity.Message;
import com.erp.erp.enums.MessageSentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByMonthAndYearAndEmailSentStatus(int month, int year, MessageSentStatus status);
}