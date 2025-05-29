package com.erp.erp.services.Impl;

import com.erp.erp.dto.response.PayslipResponseDTO;
import com.erp.erp.entity.*;
import com.erp.erp.enums.MessageSentStatus;
import com.erp.erp.enums.PayslipStatus;
import com.erp.erp.exceptions.ResourceNotFoundException;
import com.erp.erp.repository.*;
import com.erp.erp.services.EmailService;
import com.erp.erp.services.EmploymentService;
import com.erp.erp.services.PayRollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.erp.erp.repository.EmploymentRepository;
import com.erp.erp.entity.PaySlip;
import com.erp.erp.entity.Message;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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

    @Override
    @Transactional
    public List<PayslipResponseDTO> approvePayroll(int month, int year) {
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
        }

        // After all payslips are marked as PAID and triggers have fired, process notifications
        processAndSendSalaryNotifications(month, year);

        return approvedPayslipsDTOs;
    }

    @Override
    @Transactional // Ensure atomicity of email sending attempts and status updates
    public void processAndSendSalaryNotifications(int month, int year) {
        // Wait a brief moment for triggers to complete and messages to be inserted.
        // In a real-world scenario, a more robust mechanism like a delayed job or message queue might be better
        // if there's a significant chance of race conditions or heavy load.
        try {
            Thread.sleep(1000); // Small delay; adjust or remove based on testing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for messages: {}", e.getMessage());
        }


        List<Message> unsentMessages = messageRepository.findByMonthAndYearAndEmailSentStatus(month, year, MessageSentStatus.UNSENT);
        logger.info("Found {} unsent messages for {}-{}", unsentMessages.size(), month, year);

        for (Message message : unsentMessages) {
            Employee employee = employeeRepository.findByCode(message.getEmployee().getCode())
                    .orElse(null); // Message entity should have employee relation

            if (employee != null && employee.getEmail() != null) {
                try {
                    message.setEmailSentStatus(MessageSentStatus.PROCESSING);
                    messageRepository.save(message); // Mark as processing

                    emailService.sendHtmlEmail(
                            employee.getEmail(),
                            "Salary Credited - " + month + "/" + year,
                            message.getMessageContent()
                    );
                    message.setEmailSentStatus(MessageSentStatus.SENT);
                    logger.info("Salary notification email sent to {} for employee {}", employee.getEmail(), employee.getCode());
                } catch (Exception e) {
                    logger.error("Failed to send salary notification email to {}: {}", employee.getEmail(), e.getMessage());
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
}