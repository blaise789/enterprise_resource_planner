package com.erp.erp.services;

import com.erp.erp.dto.response.PayslipResponseDTO;
import java.util.List;

public interface PayRollService {
    List<PayslipResponseDTO> generatePayroll(int month, int year);
    List<PayslipResponseDTO> approvePayroll(int month, int year);
    PayslipResponseDTO getPayslipForEmployee(String employeeCode, int month, int year);
    List<PayslipResponseDTO> getAllPayslipsForMonthYear(int month, int year);
    void processAndSendSalaryNotifications(int month, int year);
    void processUnsentAndFailedEmails();
}
