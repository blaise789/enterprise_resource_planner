    package com.erp.erp.services.Impl;

    import com.erp.erp.dto.response.PayslipResponseDTO;
    import com.erp.erp.entity.*;
    import com.erp.erp.enums.MessageSentStatus;
    import com.erp.erp.enums.PayslipStatus;
    import com.erp.erp.exceptions.EmailException;
    import com.erp.erp.exceptions.ResourceNotFoundException;
    import com.erp.erp.exceptions.ValidationException;
    import com.erp.erp.repository.*;
    import com.erp.erp.services.EmailService;
    import com.erp.erp.services.EmploymentService;
    import com.erp.erp.services.PayRollService;
    import com.erp.erp.services.PayslipPdfService;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.BeanUtils;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.core.io.InputStreamResource;
    import org.springframework.core.io.InputStreamSource;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.util.StringUtils;
    import com.erp.erp.repository.EmploymentRepository;
    import com.erp.erp.entity.PaySlip;
    import com.erp.erp.entity.Message;
    import java.io.ByteArrayInputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import java.time.LocalDate;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.List;
    import java.util.Map;
    import java.util.stream.Collectors;

    @Service
    public class PayRollServiceImpl implements PayRollService {

        private static final Logger logger = LoggerFactory.getLogger(PayRollServiceImpl.class);

        @Autowired
        private EmploymentService employmentService;

        @Autowired
        private DeductionRepository deductionRepository;

        @Autowired
        private PayslipRepository payslipRepository;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private EmploymentRepository employmentRepository;

        @Autowired
        private MessageRepository messageRepository;

        @Autowired
        private EmailService emailService;

        private static final String HOUSING = "Housing";
        private static final String TRANSPORT = "Transport";
        private static final String EMPLOYEE_TAX = "Employee Tax";
        private static final String PENSION = "Pension";
        private static final String MEDICAL_INSURANCE = "Medical Insurance";
        private static final String OTHERS = "Others";


        @Override
        @Transactional
        public List<PayslipResponseDTO> generatePayroll(int month, int year) {
            validateMonthAndYear(month, year);

            List<Employment> activeEmployments = employmentService.getActiveEmploymentsForPayroll();
            List<Deduction> allDeductionsList = deductionRepository.findAll();
            Map<String, BigDecimal> deductionsMap = allDeductionsList.stream()
                    .collect(Collectors.toMap(Deduction::getDeductionName, Deduction::getPercentage));

            List<PayslipResponseDTO> generatedPayslips = new ArrayList<>();

            for (Employment employment : activeEmployments) {
                Employee employee = employment.getEmployee();
                if (payslipRepository.existsByEmployeeAndMonthAndYear(employee, month, year)) {
                    logger.warn("Payslip already exists for employee {} for {}-{}. Skipping.", employee.getCode(), month, year);
                    // Optionally, you could fetch and return existing DTO or throw an error
                    PaySlip existingPayslip = payslipRepository.findByEmployeeAndMonthAndYear(employee, month, year)
                            .orElseThrow(() -> new IllegalStateException("Payslip reported as existing but not found."));
                    generatedPayslips.add(convertToPayslipResponseDTO(existingPayslip, employee.getFirstName() + " " + employee.getLastName(), employment.getBaseSalary()));
                    continue;
                }

                BigDecimal baseSalary = employment.getBaseSalary();
                PaySlip payslip = new PaySlip();
                payslip.setEmployee(employee);
                payslip.setMonth(month);
                payslip.setYear(year);
                payslip.setStatus(PayslipStatus.PENDING);

                // Calculate amounts
                BigDecimal housingPercentage = deductionsMap.getOrDefault(HOUSING, BigDecimal.ZERO);
                BigDecimal transportPercentage = deductionsMap.getOrDefault(TRANSPORT, BigDecimal.ZERO);

                payslip.setHouseAmount(baseSalary.multiply(housingPercentage).setScale(2, RoundingMode.HALF_UP));
                payslip.setTransportAmount(baseSalary.multiply(transportPercentage).setScale(2, RoundingMode.HALF_UP));

                BigDecimal grossSalary = baseSalary.add(payslip.getHouseAmount()).add(payslip.getTransportAmount());
                payslip.setGrossSalary(grossSalary.setScale(2, RoundingMode.HALF_UP));

                payslip.setEmployeeTaxedAmount(baseSalary.multiply(deductionsMap.getOrDefault(EMPLOYEE_TAX, BigDecimal.ZERO)).setScale(2, RoundingMode.HALF_UP));
                payslip.setPensionAmount(baseSalary.multiply(deductionsMap.getOrDefault(PENSION, BigDecimal.ZERO)).setScale(2, RoundingMode.HALF_UP));
                payslip.setMedicalInsuranceAmount(baseSalary.multiply(deductionsMap.getOrDefault(MEDICAL_INSURANCE, BigDecimal.ZERO)).setScale(2, RoundingMode.HALF_UP));
                payslip.setOtherTaxedAmount(baseSalary.multiply(deductionsMap.getOrDefault(OTHERS, BigDecimal.ZERO)).setScale(2, RoundingMode.HALF_UP));

                BigDecimal totalDeductions = payslip.getEmployeeTaxedAmount()
                        .add(payslip.getPensionAmount())
                        .add(payslip.getMedicalInsuranceAmount())
                        .add(payslip.getOtherTaxedAmount());

                // Ensure deductions do not exceed gross salary (though typically based on base salary)
                if (totalDeductions.compareTo(grossSalary) > 0) {
                    logger.warn("Total deductions ({}) exceed gross salary ({}) for employee {}. Capping deductions.",
                            totalDeductions, grossSalary, employee.getCode());
                    // This scenario needs careful handling based on business rules.
                    // For now, we proceed, but a real system might require adjustment logic.
                }

                BigDecimal netSalary = grossSalary.subtract(totalDeductions);
                payslip.setNetSalary(netSalary.setScale(2, RoundingMode.HALF_UP));

                PaySlip savedPayslip = payslipRepository.save(payslip);
                generatedPayslips.add(convertToPayslipResponseDTO(savedPayslip, employee.getFirstName() + " " + employee.getLastName(), baseSalary));
            }
            return generatedPayslips;
        }

        @Autowired
        private PayslipPdfService payslipPdfService;

        @Override
        @Transactional
        public List<PayslipResponseDTO> approvePayroll(int month, int year) {
            validateMonthAndYear(month, year);

            List<PaySlip> pendingPayslips = payslipRepository.findByMonthAndYearAndStatus(month, year, PayslipStatus.PENDING);
            if (pendingPayslips.isEmpty()) {
                logger.info("No pending payslips found for approval for {}-{}", month, year);
                return new ArrayList<>();
            }

            List<PayslipResponseDTO> approvedPayslipsDTOs = new ArrayList<>();
            for (PaySlip payslip : pendingPayslips) {
                payslip.setStatus(PayslipStatus.PAID);
                PaySlip savedPayslip = payslipRepository.save(payslip); // This save triggers the DB trigger

                // Retrieve employment for base salary (optional, could be stored in payslip if needed)
                Employment employment = employmentRepository.findByEmployeeCode(payslip.getEmployee().getCode())
                        .orElse(null); // Handle if employment is not found, though unlikely for an existing payslip
                BigDecimal baseSalary = employment != null ? employment.getBaseSalary() : BigDecimal.ZERO;

                approvedPayslipsDTOs.add(convertToPayslipResponseDTO(savedPayslip,
                        payslip.getEmployee().getFirstName() + " " + payslip.getEmployee().getLastName(),
                        baseSalary));

                // Send email with PDF attachment immediately
                try {
                    // Wait a moment for the trigger to create the message
                    Thread.sleep(100);

                    // Get the employee
                    Employee employee = payslip.getEmployee();

                    // Check if employee has a valid email
                    if (employee != null && StringUtils.hasText(employee.getEmail()) && 
                        employee.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {

                        // Generate PDF payslip
                        InputStream pdfPayslip = payslipPdfService.generatePayslipPdf(employee.getCode(), month, year);

                        // Get the message content from the database (created by the trigger)
                        List<Message> messages = messageRepository.findByEmployeeCodeAndMonthAndYearAndEmailSentStatus(
                                employee.getCode(), month, year, MessageSentStatus.UNSENT);

                        if (!messages.isEmpty()) {
                            Message message = messages.get(0);
                            message.setEmailSentStatus(MessageSentStatus.PROCESSING);
                            messageRepository.save(message);

                            // Read the PDF content into a byte array
                            byte[] pdfBytes;
                            try {
                                pdfBytes = pdfPayslip.readAllBytes();
                            } catch (IOException e) {
                                logger.error("Error reading PDF content for employee {}", employee.getCode());
                                throw new RuntimeException("Failed to read PDF content", e);
                            }
                            // Create a wrapper that returns a new ByteArrayInputStream each time
                            InputStreamSource attachmentSource = new InputStreamSource() {
                                @Override
                                public InputStream getInputStream() {
                                    return new ByteArrayInputStream(pdfBytes);
                                }
                            };

                            // Process the payslip template to generate HTML content
                            String htmlContent = emailService.processPayslipTemplate(
                                    employee, 
                                    payslip, 
                                    employment, 
                                    month, 
                                    year
                            );

                            // Send email with PDF attachment
                            emailService.sendEmailWithAttachment(
                                    employee.getEmail(),
                                    "Salary Credited - " + month + "/" + year,
                                    htmlContent,
                                    "payslip_" + employee.getCode() + "_" + month + "_" + year + ".pdf",
                                    attachmentSource,
                                    "application/pdf"
                            );

                            message.setEmailSentStatus(MessageSentStatus.SENT);
                            messageRepository.save(message);
                            logger.info("Salary notification email with PDF payslip sent to {} for employee {}", 
                                    employee.getEmail(), employee.getCode());
                        } else {
                            logger.warn("No message found for employee {} for {}/{}", employee.getCode(), month, year);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error sending immediate email notification to employee {}: {}", 
                            payslip.getEmployee().getCode(), e.getMessage());
                    // Continue processing other payslips even if one email fails
                }
            }

            // Process any remaining unsent or failed emails
            processUnsentAndFailedEmails();

            return approvedPayslipsDTOs;
        }

        @Override
        @Transactional // Ensure atomicity of email sending attempts and status updates
        public void processAndSendSalaryNotifications(int month, int year) {
            validateMonthAndYear(month, year);

            List<Message> unsentMessages = messageRepository.findByMonthAndYearAndEmailSentStatus(month, year, MessageSentStatus.UNSENT);
            logger.info("Found {} unsent messages for {}-{}", unsentMessages.size(), month, year);

            for (Message message : unsentMessages) {
                Employee employee = employeeRepository.findByCode(message.getEmployee().getCode())
                        .orElse(null); // Message entity should have employee relation

                if (employee != null && StringUtils.hasText(employee.getEmail())) {
                    try {
                        message.setEmailSentStatus(MessageSentStatus.PROCESSING);
                        messageRepository.save(message); // Mark as processing

                        // Validate email format before sending
                        if (!employee.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                            logger.warn("Invalid email format for employee {}: {}. Marking as FAILED.", employee.getCode(), employee.getEmail());
                            message.setEmailSentStatus(MessageSentStatus.FAILED);
                        } else {
                            // Get the payslip for this employee
                            PaySlip payslip = payslipRepository.findByEmployeeAndMonthAndYear(employee, month, year)
                                    .orElse(null);

                            // Get the employment for this employee
                            Employment employment = employmentRepository.findByEmployeeCode(employee.getCode())
                                    .orElse(null);

                            if (payslip != null) {
                                // Process the payslip template to generate HTML content
                                String htmlContent = emailService.processPayslipTemplate(
                                        employee, 
                                        payslip, 
                                        employment, 
                                        month, 
                                        year
                                );

                                // Send HTML email with the processed template
                                emailService.sendHtmlEmail(
                                        employee.getEmail(),
                                        "Salary Credited - " + month + "/" + year,
                                        htmlContent
                                );
                            } else {
                                // Fallback to the original message content if payslip not found
                                emailService.sendHtmlEmail(
                                        employee.getEmail(),
                                        "Salary Credited - " + month + "/" + year,
                                        message.getMessageContent()
                                );
                            }
                            message.setEmailSentStatus(MessageSentStatus.SENT);
                            logger.info("Salary notification email sent to {} for employee {}", employee.getEmail(), employee.getCode());
                        }
                    } catch (ValidationException e) {
                        logger.warn("Validation error when sending email to {}: {}", employee.getEmail(), e.getMessage());
                        message.setEmailSentStatus(MessageSentStatus.FAILED);
                    } catch (EmailException e) {
                        logger.error("Email service error when sending to {}: {}", employee.getEmail(), e.getMessage());
                        message.setEmailSentStatus(MessageSentStatus.FAILED);
                    } catch (Exception e) {
                        logger.error("Unexpected error sending salary notification email to {}: {}", employee.getEmail(), e.getMessage());
                        message.setEmailSentStatus(MessageSentStatus.FAILED);
                    }
                } else {
                    logger.warn("Employee or email not found for message ID {}. Marking as FAILED.", message.getId());
                    message.setEmailSentStatus(MessageSentStatus.FAILED);
                }
                messageRepository.save(message); // Save final status
            }
        }


        @Override
        public PayslipResponseDTO getPayslipForEmployee(String employeeCode, int month, int year) {
            validateMonthAndYear(month, year);

            if (!StringUtils.hasText(employeeCode)) {
                throw new ValidationException("Employee code cannot be null or empty");
            }

            Employee employee = employeeRepository.findByCode(employeeCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + employeeCode));
            PaySlip payslip = payslipRepository.findByEmployeeAndMonthAndYear(employee, month, year)
                    .orElseThrow(() -> new ResourceNotFoundException("Payslip not found for employee " + employeeCode + " for " + month + "/" + year));

            Employment employment = employmentRepository.findByEmployeeCode(employeeCode).orElse(null);
            BigDecimal baseSalary = employment != null ? employment.getBaseSalary() : BigDecimal.ZERO;

            return convertToPayslipResponseDTO(payslip, employee.getFirstName() + " " + employee.getLastName(), baseSalary);
        }

        @Override
        public List<PayslipResponseDTO> getAllPayslipsForMonthYear(int month, int year) {
            validateMonthAndYear(month, year);

            List<PaySlip> payslips = payslipRepository.findByMonthAndYear(month, year);
            return payslips.stream().map(p -> {
                Employee emp = p.getEmployee(); // Assumes Employee is fetched (Lazy might require transaction or fetch join)
                Employment employment = employmentRepository.findByEmployeeCode(emp.getCode()).orElse(null);
                BigDecimal baseSalary = employment != null ? employment.getBaseSalary() : BigDecimal.ZERO;
                return convertToPayslipResponseDTO(p, emp.getFirstName() + " " + emp.getLastName(), baseSalary);
            }).collect(Collectors.toList());
        }

        private PayslipResponseDTO convertToPayslipResponseDTO(PaySlip payslip, String employeeName, BigDecimal baseSalary) {
            PayslipResponseDTO dto = new PayslipResponseDTO();
            BeanUtils.copyProperties(payslip, dto);
            dto.setEmployeeCode(payslip.getEmployee().getCode());
            dto.setEmployeeName(employeeName);
            dto.setBaseSalary(baseSalary.setScale(2, RoundingMode.HALF_UP)); // Add base salary to DTO
            return dto;
        }

        /**
         * Validates that month and year parameters are within valid ranges.
         *
         * @param month the month (1-12)
         * @param year the year
         * @throws ValidationException if month or year is invalid
         */
        private void validateMonthAndYear(int month, int year) {
            if (month < 1 || month > 12) {
                throw new ValidationException("Month must be between 1 and 12, got: " + month);
            }

            int currentYear = LocalDate.now().getYear();
            if (year < 2000 || year > currentYear + 5) {
                throw new ValidationException("Year must be between 2000 and " + (currentYear + 5) + ", got: " + year);
            }
        }

        @Override
        @Transactional
        public void processUnsentAndFailedEmails() {

            // Find all messages with status UNSENT or FAILED
            List<MessageSentStatus> statuses = Arrays.asList(MessageSentStatus.UNSENT, MessageSentStatus.FAILED);
            List<Message> unprocessedMessages = new ArrayList<>();

            // For each status, find messages and add them to the list
            for (MessageSentStatus status : statuses) {

                for (int month = 1; month <= 12; month++) {
                    int currentYear = LocalDate.now().getYear();
                    for (int year = currentYear - 1; year <= currentYear; year++) {
                        List<Message> messages = messageRepository.findByMonthAndYearAndEmailSentStatus(month, year, status);
                        unprocessedMessages.addAll(messages);
                    }
                }
            }

            logger.info("Found {} unsent or failed messages to process", unprocessedMessages.size());

            // Process each message
            for (Message message : unprocessedMessages) {
                Employee employee = employeeRepository.findByCode(message.getEmployee().getCode())
                        .orElse(null);

                if (employee != null && StringUtils.hasText(employee.getEmail())) {
                    try {
                        message.setEmailSentStatus(MessageSentStatus.PROCESSING);
                        messageRepository.save(message); // Mark as processing

                        // Validate email format before sending
                        if (!employee.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                            logger.warn("Invalid email format for employee {}: {}. Marking as FAILED.", employee.getCode(), employee.getEmail());
                            message.setEmailSentStatus(MessageSentStatus.FAILED);
                        } else {
                            // Get the payslip for this employee
                            PaySlip payslip = payslipRepository.findByEmployeeAndMonthAndYear(
                                    employee, message.getMonth(), message.getYear())
                                    .orElse(null);

                            // Get the employment for this employee
                            Employment employment = employmentRepository.findByEmployeeCode(employee.getCode())
                                    .orElse(null);

                            if (payslip != null) {
                                // Process the payslip template to generate HTML content
                                String htmlContent = emailService.processPayslipTemplate(
                                        employee, 
                                        payslip, 
                                        employment, 
                                        message.getMonth(), 
                                        message.getYear()
                                );

                                // Send HTML email with the processed template
                                emailService.sendHtmlEmail(
                                        employee.getEmail(),
                                        "Salary Credited - " + message.getMonth() + "/" + message.getYear(),
                                        htmlContent
                                );
                            } else {
                                // Fallback to the original message content if payslip not found
                                emailService.sendHtmlEmail(
                                        employee.getEmail(),
                                        "Salary Credited - " + message.getMonth() + "/" + message.getYear(),
                                        message.getMessageContent()
                                );
                            }
                            message.setEmailSentStatus(MessageSentStatus.SENT);
                            logger.info("Salary notification email sent to {} for employee {}", employee.getEmail(), employee.getCode());
                        }
                    } catch (ValidationException e) {
                        logger.warn("Validation error when sending email to {}: {}", employee.getEmail(), e.getMessage());
                        message.setEmailSentStatus(MessageSentStatus.FAILED);
                    } catch (EmailException e) {
                        logger.error("Email service error when sending to {}: {}", employee.getEmail(), e.getMessage());
                        message.setEmailSentStatus(MessageSentStatus.FAILED);
                    } catch (Exception e) {
                        logger.error("Unexpected error sending salary notification email to {}: {}", employee.getEmail(), e.getMessage());
                        message.setEmailSentStatus(MessageSentStatus.FAILED);
                    }
                } else {
                    logger.warn("Employee or email not found for message ID {}. Marking as FAILED.", message.getId());
                    message.setEmailSentStatus(MessageSentStatus.FAILED);
                }
                messageRepository.save(message); // Save final status
            }

            logger.info("Finished processing unsent and failed emails");
        }
    }
