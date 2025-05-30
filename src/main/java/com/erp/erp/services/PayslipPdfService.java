package com.erp.erp.services;

import com.erp.erp.entity.Employee;
import com.erp.erp.entity.Employment;
import com.erp.erp.entity.PaySlip;
import com.erp.erp.exceptions.ResourceNotFoundException;
import com.erp.erp.repository.EmployeeRepository;
import com.erp.erp.repository.EmploymentRepository;
import com.erp.erp.repository.PayslipRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PayslipPdfService {

    private static final Logger logger = LoggerFactory.getLogger(PayslipPdfService.class);

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmploymentRepository employmentRepository;

    /**
     * Generates a PDF payslip for the given employee, month, and year
     * Enhanced version with better formatting and more descriptive content
     *
     * @param employeeCode the employee code
     * @param month the month (1-12)
     * @param year the year
     * @return an InputStream containing the PDF data
     */
    public InputStream generatePayslipPdf(String employeeCode, int month, int year) {
        try {
            // Fetch the required data
            Employee employee = employeeRepository.findByCode(employeeCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + employeeCode));

            PaySlip payslip = payslipRepository.findByEmployeeAndMonthAndYear(employee, month, year)
                    .orElseThrow(() -> new ResourceNotFoundException("Payslip not found for employee " + employeeCode + " for " + month + "/" + year));

            Employment activeEmployment = employmentRepository.findByEmployeeCode(employeeCode)
                    .orElse(null);

            // Format currency values
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "RW"));
            currencyFormat.setMaximumFractionDigits(2);
            currencyFormat.setMinimumFractionDigits(2);

            // Create PDF document
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Set fonts
            PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font italicFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            // Define colors
            Color primaryColor = new Color(0, 102, 204); // Blue
            Color secondaryColor = new Color(51, 51, 51); // Dark gray
            Color highlightColor = new Color(0, 153, 0); // Green for positive values
            Color warningColor = new Color(204, 0, 0); // Red for negative values

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float pageWidth = page.getMediaBox().getWidth();
            float contentWidth = pageWidth - 2 * margin;
            float yPosition = yStart;
            float leading = 15;
            float columnWidth = contentWidth / 2;

            // Calculate the pay period
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

            // ===== HEADER SECTION =====
            // Draw header background
            contentStream.setNonStrokingColor(primaryColor);
            contentStream.addRect(margin, yPosition - 30, contentWidth, 40);
            contentStream.fill();

            // Title
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(titleFont, 18);
            contentStream.newLineAtOffset(margin + 10, yPosition - 20);
            contentStream.showText("PAYSLIP");
            contentStream.endText();

            // Company name
            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(headerFont, 14);
            contentStream.newLineAtOffset(pageWidth - margin - 200, yPosition - 20);
            contentStream.showText("Government of Rwanda");
            contentStream.endText();
            yPosition -= 50;

            // Pay period
            contentStream.beginText();
            contentStream.setNonStrokingColor(secondaryColor);
            contentStream.setFont(headerFont, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Pay Period: " + startDate.format(dateFormatter) + " to " + endDate.format(dateFormatter));
            contentStream.endText();
            yPosition -= leading * 1.5f;

            // Draw separator line
            contentStream.setStrokingColor(primaryColor);
            contentStream.setLineWidth(1f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(pageWidth - margin, yPosition);
            contentStream.stroke();
            yPosition -= leading;

            // ===== EMPLOYEE INFORMATION SECTION =====
            // Section title
            contentStream.beginText();
            contentStream.setNonStrokingColor(primaryColor);
            contentStream.setFont(headerFont, 14);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Employee Information");
            contentStream.endText();
            yPosition -= leading * 1.5f;

            // Employee details - left column
            float leftColumnX = margin;
            float rightColumnX = margin + columnWidth;

            // Employee name
            contentStream.beginText();
            contentStream.setNonStrokingColor(secondaryColor);
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(leftColumnX, yPosition);
            contentStream.showText("Employee Name:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 11);
            contentStream.newLineAtOffset(leftColumnX + 120, yPosition);
            contentStream.showText(employee.getFirstName() + " " + employee.getLastName());
            contentStream.endText();

            // Employee ID
            contentStream.beginText();
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(rightColumnX, yPosition);
            contentStream.showText("Employee ID:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 11);
            contentStream.newLineAtOffset(rightColumnX + 100, yPosition);
            contentStream.showText(employee.getCode());
            contentStream.endText();
            yPosition -= leading;

            // Email
            contentStream.beginText();
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(leftColumnX, yPosition);
            contentStream.showText("Email:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 11);
            contentStream.newLineAtOffset(leftColumnX + 120, yPosition);
            contentStream.showText(employee.getEmail());
            contentStream.endText();

            // Mobile
            contentStream.beginText();
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(rightColumnX, yPosition);
            contentStream.showText("Mobile:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 11);
            contentStream.newLineAtOffset(rightColumnX + 100, yPosition);
            contentStream.showText(employee.getMobile() != null ? employee.getMobile() : "N/A");
            contentStream.endText();
            yPosition -= leading * 1.5f;

            // Employment details if available
            if (activeEmployment != null) {
                contentStream.beginText();
                contentStream.setNonStrokingColor(primaryColor);
                contentStream.setFont(headerFont, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Employment Details");
                contentStream.endText();
                yPosition -= leading;

                // Department
                contentStream.beginText();
                contentStream.setNonStrokingColor(secondaryColor);
                contentStream.setFont(headerFont, 11);
                contentStream.newLineAtOffset(leftColumnX, yPosition);
                contentStream.showText("Department:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 11);
                contentStream.newLineAtOffset(leftColumnX + 120, yPosition);
                contentStream.showText(activeEmployment.getDepartment());
                contentStream.endText();

                // Position
                contentStream.beginText();
                contentStream.setFont(headerFont, 11);
                contentStream.newLineAtOffset(rightColumnX, yPosition);
                contentStream.showText("Position:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 11);
                contentStream.newLineAtOffset(rightColumnX + 100, yPosition);
                contentStream.showText(activeEmployment.getPosition());
                contentStream.endText();
                yPosition -= leading;

                // Joining date
                contentStream.beginText();
                contentStream.setFont(headerFont, 11);
                contentStream.newLineAtOffset(leftColumnX, yPosition);
                contentStream.showText("Joining Date:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 11);
                contentStream.newLineAtOffset(leftColumnX + 120, yPosition);
                String joiningDate = activeEmployment.getJoiningDate() != null ? 
                    activeEmployment.getJoiningDate().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "N/A";
                contentStream.showText(joiningDate);
                contentStream.endText();

                // Base Salary
                contentStream.beginText();
                contentStream.setFont(headerFont, 11);
                contentStream.newLineAtOffset(rightColumnX, yPosition);
                contentStream.showText("Base Salary:");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 11);
                contentStream.newLineAtOffset(rightColumnX + 100, yPosition);
                contentStream.showText(currencyFormat.format(activeEmployment.getBaseSalary()));
                contentStream.endText();
                yPosition -= leading * 1.5f;
            }

            // Draw separator line
            contentStream.setStrokingColor(primaryColor);
            contentStream.setLineWidth(1f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(pageWidth - margin, yPosition);
            contentStream.stroke();
            yPosition -= leading * 1.5f;

            // ===== EARNINGS SECTION =====
            // Section title
            contentStream.beginText();
            contentStream.setNonStrokingColor(primaryColor);
            contentStream.setFont(headerFont, 14);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Earnings");
            contentStream.endText();
            yPosition -= leading;

            // Draw table header
            contentStream.setNonStrokingColor(primaryColor.brighter());
            contentStream.addRect(margin, yPosition - 15, contentWidth, 20);
            contentStream.fill();

            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(margin + 10, yPosition - 10);
            contentStream.showText("Description");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition - 10);
            contentStream.showText("Amount (RWF)");
            contentStream.endText();
            yPosition -= 25;

            // Base salary
            contentStream.beginText();
            contentStream.setNonStrokingColor(secondaryColor);
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin + 10, yPosition);
            contentStream.showText("Base Salary");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition);
            contentStream.showText(activeEmployment != null ? 
                currencyFormat.format(activeEmployment.getBaseSalary()) : "N/A");
            contentStream.endText();
            yPosition -= leading;

            // Housing allowance
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin + 10, yPosition);
            contentStream.showText("Housing Allowance");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition);
            contentStream.showText(currencyFormat.format(payslip.getHouseAmount()));
            contentStream.endText();
            yPosition -= leading;

            // Transport allowance
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin + 10, yPosition);
            contentStream.showText("Transport Allowance");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition);
            contentStream.showText(currencyFormat.format(payslip.getTransportAmount()));
            contentStream.endText();
            yPosition -= leading;

            // Draw total earnings line
            contentStream.setStrokingColor(secondaryColor);
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(margin, yPosition - 5);
            contentStream.lineTo(pageWidth - margin, yPosition - 5);
            contentStream.stroke();
            yPosition -= 20;

            // Total earnings
            contentStream.beginText();
            contentStream.setNonStrokingColor(highlightColor);
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(margin + 10, yPosition);
            contentStream.showText("Total Earnings");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setNonStrokingColor(highlightColor);
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition);
            contentStream.showText(currencyFormat.format(payslip.getGrossSalary()));
            contentStream.endText();
            yPosition -= leading * 2;

            // ===== DEDUCTIONS SECTION =====
            // Section title
            contentStream.beginText();
            contentStream.setNonStrokingColor(primaryColor);
            contentStream.setFont(headerFont, 14);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Deductions");
            contentStream.endText();
            yPosition -= leading;

            // Draw table header
            contentStream.setNonStrokingColor(primaryColor.brighter());
            contentStream.addRect(margin, yPosition - 15, contentWidth, 20);
            contentStream.fill();

            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(margin + 10, yPosition - 10);
            contentStream.showText("Description");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition - 10);
            contentStream.showText("Amount (RWF)");
            contentStream.endText();
            yPosition -= 25;

            // Employee tax
            contentStream.beginText();
            contentStream.setNonStrokingColor(secondaryColor);
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin + 10, yPosition);
            contentStream.showText("Employee Tax");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition);
            contentStream.showText(currencyFormat.format(payslip.getEmployeeTaxedAmount()));
            contentStream.endText();
            yPosition -= leading;

            // Pension
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin + 10, yPosition);
            contentStream.showText("Pension");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition);
            contentStream.showText(currencyFormat.format(payslip.getPensionAmount()));
            contentStream.endText();
            yPosition -= leading;

            // Medical insurance
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin + 10, yPosition);
            contentStream.showText("Medical Insurance");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition);
            contentStream.showText(currencyFormat.format(payslip.getMedicalInsuranceAmount()));
            contentStream.endText();
            yPosition -= leading;

            // Other deductions
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin + 10, yPosition);
            contentStream.showText("Other Deductions");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition);
            contentStream.showText(currencyFormat.format(payslip.getOtherTaxedAmount()));
            contentStream.endText();
            yPosition -= leading;

            // Calculate total deductions
            BigDecimal totalDeductions = payslip.getEmployeeTaxedAmount()
                    .add(payslip.getPensionAmount())
                    .add(payslip.getMedicalInsuranceAmount())
                    .add(payslip.getOtherTaxedAmount());

            // Draw total deductions line
            contentStream.setStrokingColor(secondaryColor);
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(margin, yPosition - 5);
            contentStream.lineTo(pageWidth - margin, yPosition - 5);
            contentStream.stroke();
            yPosition -= 20;

            // Total deductions
            contentStream.beginText();
            contentStream.setNonStrokingColor(warningColor);
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(margin + 10, yPosition);
            contentStream.showText("Total Deductions");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setNonStrokingColor(warningColor);
            contentStream.setFont(headerFont, 11);
            contentStream.newLineAtOffset(pageWidth - margin - 100, yPosition);
            contentStream.showText(currencyFormat.format(totalDeductions));
            contentStream.endText();
            yPosition -= leading * 2;

            // ===== SUMMARY SECTION =====
            // Draw summary box
            contentStream.setNonStrokingColor(new Color(240, 240, 240));
            contentStream.addRect(margin, yPosition - 80, contentWidth, 80);
            contentStream.fill();

            // Summary title
            contentStream.beginText();
            contentStream.setNonStrokingColor(primaryColor);
            contentStream.setFont(headerFont, 14);
            contentStream.newLineAtOffset(margin + 10, yPosition - 20);
            contentStream.showText("Payment Summary");
            contentStream.endText();
            yPosition -= 40;

            // Gross salary
            contentStream.beginText();
            contentStream.setNonStrokingColor(secondaryColor);
            contentStream.setFont(normalFont, 11);
            contentStream.newLineAtOffset(margin + 20, yPosition);
            contentStream.showText("Gross Salary:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 11);
            contentStream.newLineAtOffset(margin + 150, yPosition);
            contentStream.showText(currencyFormat.format(payslip.getGrossSalary()));
            contentStream.endText();
            yPosition -= leading;

            // Total deductions
            contentStream.beginText();
            contentStream.setFont(normalFont, 11);
            contentStream.newLineAtOffset(margin + 20, yPosition);
            contentStream.showText("Total Deductions:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 11);
            contentStream.newLineAtOffset(margin + 150, yPosition);
            contentStream.showText(currencyFormat.format(totalDeductions));
            contentStream.endText();
            yPosition -= leading;

            // Net salary
            contentStream.beginText();
            contentStream.setNonStrokingColor(highlightColor);
            contentStream.setFont(headerFont, 12);
            contentStream.newLineAtOffset(margin + 20, yPosition);
            contentStream.showText("Net Salary:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setNonStrokingColor(highlightColor);
            contentStream.setFont(headerFont, 12);
            contentStream.newLineAtOffset(margin + 150, yPosition);
            contentStream.showText(currencyFormat.format(payslip.getNetSalary()));
            contentStream.endText();
            yPosition -= 50;

            // ===== PAYMENT STATUS =====
            // Draw status box
            contentStream.setNonStrokingColor(payslip.getStatus().name().equals("PAID") ? 
                new Color(230, 255, 230) : new Color(255, 230, 230));
            contentStream.addRect(margin, yPosition - 30, contentWidth, 30);
            contentStream.fill();

            // Status text
            contentStream.beginText();
            contentStream.setNonStrokingColor(payslip.getStatus().name().equals("PAID") ? 
                new Color(0, 120, 0) : new Color(180, 0, 0));
            contentStream.setFont(headerFont, 12);
            contentStream.newLineAtOffset(margin + contentWidth/2 - 50, yPosition - 20);
            contentStream.showText("Status: " + payslip.getStatus().name());
            contentStream.endText();
            yPosition -= 50;

            // ===== FOOTER =====
            // Draw footer line
            contentStream.setStrokingColor(primaryColor);
            contentStream.setLineWidth(1f);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(pageWidth - margin, yPosition);
            contentStream.stroke();
            yPosition -= 20;

            // Generated date
            contentStream.beginText();
            contentStream.setNonStrokingColor(secondaryColor);
            contentStream.setFont(normalFont, 9);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
            contentStream.endText();

            // Disclaimer
            contentStream.beginText();
            contentStream.setFont(italicFont, 9);
            contentStream.newLineAtOffset(margin, yPosition - 15);
            contentStream.showText("This is an electronic payslip and does not require a signature. For any queries, please contact HR department.");
            contentStream.endText();

            contentStream.close();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            document.close();

            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException e) {
            logger.error("Error generating PDF payslip for employee {}: {}", employeeCode, e.getMessage());
            throw new RuntimeException("Failed to generate PDF payslip: " + e.getMessage(), e);
        }
    }
}
