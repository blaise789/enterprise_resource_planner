//package com.erp.erp.config;
//
//import com.erp.erp.services.PayRollService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//
//@Configuration
//@EnableScheduling
//public class SchedulerConfig {
//
//    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
//
//    @Autowired
//    private PayRollService payRollService;
//
//    /**
//     * Scheduled task to process unsent and failed emails.
//     * Runs every 15 minutes.
//     */
//    @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
//    public void scheduleEmailProcessing() {
//        logger.info("Starting scheduled email processing task");
//        payRollService.processUnsentAndFailedEmails();
//        logger.info("Completed scheduled email processing task");
//    }
//}