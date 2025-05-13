package com.lunar.stripelunar.scheduler;

import com.lunar.stripelunar.model.ETLJobHistory;
import com.lunar.stripelunar.notification.NotificationService;
import com.lunar.stripelunar.repository.ETLJobHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for generating and sending ETL job summaries
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobSummaryScheduler {

    private final ETLJobHistoryRepository etlJobHistoryRepository;
    private final NotificationService notificationService;
    
    @Value("${scheduler.job-summary.enabled:true}")
    private boolean jobSummaryEnabled;
    
    /**
     * Generate and send daily job summary
     * Runs at 6:00 AM every day
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void generateDailyJobSummary() {
        if (!jobSummaryEnabled) {
            log.debug("Job summary scheduler is disabled. Skipping daily job summary.");
            return;
        }
        
        log.info("Generating daily ETL job summary");
        
        // Get jobs from the last 24 hours
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<ETLJobHistory> recentJobs = etlJobHistoryRepository.findJobsAfterDate(yesterday);
        
        generateAndSendSummary("Daily", recentJobs);
    }
    
    /**
     * Generate and send weekly job summary
     * Runs at 7:00 AM every Monday
     */
    @Scheduled(cron = "0 0 7 * * MON")
    public void generateWeeklyJobSummary() {
        if (!jobSummaryEnabled) {
            log.debug("Job summary scheduler is disabled. Skipping weekly job summary.");
            return;
        }
        
        log.info("Generating weekly ETL job summary");
        
        // Get jobs from the last 7 days
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        List<ETLJobHistory> recentJobs = etlJobHistoryRepository.findJobsAfterDate(lastWeek);
        
        generateAndSendSummary("Weekly", recentJobs);
    }
    
    /**
     * Generate and send monthly job summary
     * Runs at 8:00 AM on the 1st day of each month
     */
    @Scheduled(cron = "0 0 8 1 * ?")
    public void generateMonthlyJobSummary() {
        if (!jobSummaryEnabled) {
            log.debug("Job summary scheduler is disabled. Skipping monthly job summary.");
            return;
        }
        
        log.info("Generating monthly ETL job summary");
        
        // Get jobs from the last 30 days
        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
        List<ETLJobHistory> recentJobs = etlJobHistoryRepository.findJobsAfterDate(lastMonth);
        
        generateAndSendSummary("Monthly", recentJobs);
    }
    
    /**
     * Generate and send job summary for a specific job type and period
     * 
     * @param period The period for the summary (Daily, Weekly, Monthly)
     * @param jobs The list of jobs to include in the summary
     */
    private void generateAndSendSummary(String period, List<ETLJobHistory> jobs) {
        // Count jobs by type and status
        int customerSyncSuccess = 0;
        int customerSyncFailed = 0;
        int paymentSyncSuccess = 0;
        int paymentSyncFailed = 0;
        int fullSyncSuccess = 0;
        int fullSyncFailed = 0;
        int totalRecordsProcessed = 0;
        
        for (ETLJobHistory job : jobs) {
            // Add to total records processed
            if (job.getRecordsProcessed() != null) {
                totalRecordsProcessed += job.getRecordsProcessed();
            }
            
            // Count by job type and status
            switch (job.getJobName()) {
                case "syncCustomers":
                    if (job.isCompleted()) {
                        customerSyncSuccess++;
                    } else if (job.isFailed()) {
                        customerSyncFailed++;
                    }
                    break;
                    
                case "syncPayments":
                    if (job.isCompleted()) {
                        paymentSyncSuccess++;
                    } else if (job.isFailed()) {
                        paymentSyncFailed++;
                    }
                    break;
                    
                case "syncAll":
                    if (job.isCompleted()) {
                        fullSyncSuccess++;
                    } else if (job.isFailed()) {
                        fullSyncFailed++;
                    }
                    break;
            }
        }
        
        // Send summary notifications for each job type
        if (customerSyncSuccess > 0 || customerSyncFailed > 0) {
            notificationService.sendJobSummaryNotification(
                period + " Customer Sync", customerSyncSuccess, customerSyncFailed, totalRecordsProcessed);
        }
        
        if (paymentSyncSuccess > 0 || paymentSyncFailed > 0) {
            notificationService.sendJobSummaryNotification(
                period + " Payment Sync", paymentSyncSuccess, paymentSyncFailed, totalRecordsProcessed);
        }
        
        if (fullSyncSuccess > 0 || fullSyncFailed > 0) {
            notificationService.sendJobSummaryNotification(
                period + " Full Sync", fullSyncSuccess, fullSyncFailed, totalRecordsProcessed);
        }
        
        log.info("{} job summary generated and sent. Total jobs: {}", period, jobs.size());
    }
}