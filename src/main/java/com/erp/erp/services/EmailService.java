package com.erp.erp.services;


public interface EmailService {
    void sendSimpleMailMessage(String to, String subject, String text);
    void sendHtmlEmail(String to, String subject, String htmlBody);
}
