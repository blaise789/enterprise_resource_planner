package com.erp.erp.dto.response;



import com.erp.erp.enums.MessageSentStatus;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDTO {
    private Long id;
    private String employeeCode;
    private String messageContent;
    private LocalDateTime sentAt;
    private int month;
    private int year;
    private MessageSentStatus emailSentStatus;
}
