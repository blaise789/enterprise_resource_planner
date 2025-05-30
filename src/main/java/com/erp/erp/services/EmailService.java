package com.erp.erp.services;

import com.erp.erp.entity.Employee;
import com.erp.erp.entity.Employment;
import com.erp.erp.entity.PaySlip;
import org.springframework.core.io.InputStreamSource;

import java.util.Map;

public interface EmailService {
    void sendSimpleMailMessage(String to, String subject, String text);
    void sendHtmlEmail(String to, String subject, String htmlBody);
    void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, InputStreamSource attachmentSource, String contentType);

    /**
     * Process a Thymeleaf template and return the processed HTML
     * 
     * @param templateName the name of the template file (without path or extension)
     * @param variables the variables to pass to the template
     * @return the processed HTML
     */
    String processTemplate(String templateName, Map<String, Object> variables);

    /**
     * Process the payslip.html template with the given data and return the processed HTML
     * 
     * @param employee the employee
     * @param payslip the payslip
     * @param activeEmployment the active employment (can be null)
     * @param month the month (1-12)
     * @param year the year
     * @return the processed HTML
     */
    String processPayslipTemplate(Employee employee, PaySlip payslip, Employment activeEmployment, int month, int year);
}
