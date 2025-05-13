package com.lunar.stripelunar.notification;

import com.lunar.stripelunar.model.ETLJobHistory;

/**
 * Service for sending notifications about ETL job events
 */
public interface NotificationService {
    
    /**
     * Send a notification about a job completion
     * 
     * @param job The completed ETL job
     */
    void sendJobCompletionNotification(ETLJobHistory job);
    
    /**
     * Send a notification about a job failure
     * 
     * @param job The failed ETL job
     */
    void sendJobFailureNotification(ETLJobHistory job);
    
    /**
     * Send a notification about a job summary
     * 
     * @param jobName The job name
     * @param successCount Number of successful jobs
     * @param failureCount Number of failed jobs
     * @param totalRecordsProcessed Total records processed
     */
    void sendJobSummaryNotification(String jobName, int successCount, int failureCount, int totalRecordsProcessed);
}
