package com.erp.erp.entity;

import com.erp.erp.enums.MessageSentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false)
    private Employee employee;

    @Column(columnDefinition = "TEXT")
    private String messageContent;
    private LocalDateTime sentAt;
    private int month; // Month of the salary
    private int year;  // Year of the salary

    @Enumerated(EnumType.STRING)
    private MessageSentStatus emailSentStatus = MessageSentStatus.UNSENT; // UNSENT, SENT, FAILED
    // Getters, Setters

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public MessageSentStatus getEmailSentStatus() { return emailSentStatus; }
    public void setEmailSentStatus(MessageSentStatus status) { this.emailSentStatus = status; }
    public Long getId() { // <--- METHOD NEEDED
        return id;
    }

    public String getMessageContent() { // <--- METHOD NEEDED
        return messageContent;
    }
}
