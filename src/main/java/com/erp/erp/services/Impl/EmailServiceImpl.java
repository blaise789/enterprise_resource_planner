package com.erp.erp.services.Impl;

import com.erp.erp.entity.Employee;
import com.erp.erp.entity.Employment;
import com.erp.erp.entity.PaySlip;
import com.erp.erp.exceptions.EmailException;
import com.erp.erp.exceptions.ValidationException;
import com.erp.erp.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import com.erp.erp.utils.CurrencyFormatter;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendSimpleMailMessage(String to, String subject, String text) {
        validateEmailParameters(to, subject, text);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            logger.info("Simple email sent successfully to {}", to);
        } catch (MailException e) {
            logger.error("Error sending simple email to {}: {}", to, e.getMessage());
            throw new EmailException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        validateEmailParameters(to, subject, htmlBody);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content
            mailSender.send(mimeMessage);
            logger.info("HTML email sent successfully to {}", to);
        } catch (MessagingException | MailException e) {
            logger.error("Error sending HTML email to {}: {}", to, e.getMessage());
            throw new EmailException("Failed to send HTML email: " + e.getMessage(), e);
        }
    }

    /**
     * Validates email parameters to ensure they are not null or empty.
     * 
     * @param to the recipient email address
     * @param subject the email subject
     * @param body the email body
     * @throws ValidationException if any parameter is null or empty
     */
    private void validateEmailParameters(String to, String subject, String body) {
        if (!StringUtils.hasText(to)) {
            throw new ValidationException("Email recipient (to) cannot be null or empty");
        }
        if (!StringUtils.hasText(subject)) {
            throw new ValidationException("Email subject cannot be null or empty");
        }
        if (!StringUtils.hasText(body)) {
            throw new ValidationException("Email body cannot be null or empty");
        }

        // Basic email format validation
        if (!to.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new ValidationException("Invalid email format: " + to);
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String text, String attachmentName, 
                                        InputStreamSource attachmentSource, String contentType) {
        validateEmailParameters(to, subject, text);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // true indicates multipart message

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // true indicates HTML content

            // Add the attachment
            helper.addAttachment(attachmentName, attachmentSource, contentType);

            mailSender.send(mimeMessage);
            logger.info("Email with attachment sent successfully to {}", to);
        } catch (MessagingException | MailException e) {
            logger.error("Error sending email with attachment to {}: {}", to, e.getMessage());
            throw new EmailException("Failed to send email with attachment: " + e.getMessage(), e);
        }
    }

    @Override
    public String processTemplate(String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            logger.error("Error processing template {}: {}", templateName, e.getMessage());
            throw new EmailException("Failed to process template: " + e.getMessage(), e);
        }
    }

    @Override
    public String processPayslipTemplate(Employee employee, PaySlip payslip, Employment activeEmployment, int month, int year) {
        try {
            Map<String, Object> variables = new HashMap<>();

            // Add employee data
            variables.put("employee", employee);

            // Add payslip data
            variables.put("payslip", payslip);

            // Add employment data
            variables.put("activeEmployment", activeEmployment);

            // Add month and year
            variables.put("month", month);
            variables.put("year", year);

            // Add month name
            String monthName = Month.of(month).toString();
            variables.put("monthName", monthName);

            // Add currency formatter utility class
            variables.put("currencyFormatter", CurrencyFormatter.class);

            // Process the template
            return processTemplate("payslip", variables);
        } catch (Exception e) {
            logger.error("Error processing payslip template for employee {}: {}", 
                    employee != null ? employee.getCode() : "null", e.getMessage());
            throw new EmailException("Failed to process payslip template: " + e.getMessage(), e);
        }
    }
}
