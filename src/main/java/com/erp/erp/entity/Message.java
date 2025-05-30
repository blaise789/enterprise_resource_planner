package com.erp.erp.entity;

import com.erp.erp.enums.MessageSentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    // <--- METHOD NEEDED
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false)
    private Employee employee;

    // <--- METHOD NEEDED
    @Getter
    @Column(columnDefinition = "TEXT")
    private String messageContent;
    private LocalDateTime sentAt;
    @Getter
    private int month; // Month of the salary
    @Getter
    private int year;  // Year of the salary

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private MessageSentStatus emailSentStatus = MessageSentStatus.UNSENT; // UNSENT, SENT, FAILED
    // Getters, Setters

}
