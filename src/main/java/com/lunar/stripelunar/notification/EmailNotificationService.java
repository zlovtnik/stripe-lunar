package com.lunar.stripelunar.notification;

import com.lunar.stripelunar.model.ETLJobHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Email implementation of the NotificationService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Value("${notification.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${notification.email.from:noreply@lunar.com}")
    private String fromEmail;
    
    @Value("${notification.email.to:admin@lunar.com}")
    private String toEmail;
    
    @Value("${notification.email.subject.prefix:[Stripe-Lunar ETL]}")
    private String subjectPrefix;

    @Override
    @Async
    public void sendJobCompletionNotification(ETLJobHistory job) {
        if (!emailEnabled) {
            log.debug("Email notifications disabled. Not sending job completion notification.");
            return;
        }
        
        log.info("Sending job completion notification for job: {}", job.getId());
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(String.format("%s Job Completed: %s", subjectPrefix, job.getJobName()));
        
        String duration = "N/A";
        if (job.getStartTime() != null && job.getEndTime() != null) {
            long seconds = java.time.Duration.between(job.getStartTime(), job.getEndTime()).getSeconds();
            duration = formatDuration(seconds);
        }
        
        String body = String.format(
            "ETL Job completed successfully.\n\n" +
            "Job Details:\n" +
            "- Job ID: %d\n" +
            "- Job Name: %s\n" +
            "- Start Time: %s\n" +
            "- End Time: %s\n" +
            "- Duration: %s\n" +
            "- Records Processed: %d\n\n" +
            "This is an automated notification from the Stripe-Lunar ETL system.",
            job.getId(),
            job.getJobName(),
            job.getStartTime() != null ? job.getStartTime().format(DATE_FORMATTER) : "N/A",
            job.getEndTime() != null ? job.getEndTime().format(DATE_FORMATTER) : "N/A",
            duration,
            job.getRecordsProcessed() != null ? job.getRecordsProcessed() : 0
        );
        
        message.setText(body);
        
        try {
            mailSender.send(message);
            log.info("Job completion notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send job completion notification", e);
        }
    }

    @Override
    @Async
    public void sendJobFailureNotification(ETLJobHistory job) {
        if (!emailEnabled) {
            log.debug("Email notifications disabled. Not sending job failure notification.");
            return;
        }
        
        log.info("Sending job failure notification for job: {}", job.getId());
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(String.format("%s Job Failed: %s", subjectPrefix, job.getJobName()));
        
        String duration = "N/A";
        if (job.getStartTime() != null && job.getEndTime() != null) {
            long seconds = java.time.Duration.between(job.getStartTime(), job.getEndTime()).getSeconds();
            duration = formatDuration(seconds);
        }
        
        String body = String.format(
            "ETL Job failed.\n\n" +
            "Job Details:\n" +
            "- Job ID: %d\n" +
            "- Job Name: %s\n" +
            "- Start Time: %s\n" +
            "- End Time: %s\n" +
            "- Duration: %s\n" +
            "- Error Message: %s\n\n" +
            "Please check the application logs for more details.\n\n" +
            "This is an automated notification from the Stripe-Lunar ETL system.",
            job.getId(),
            job.getJobName(),
            job.getStartTime() != null ? job.getStartTime().format(DATE_FORMATTER) : "N/A",
            job.getEndTime() != null ? job.getEndTime().format(DATE_FORMATTER) : "N/A",
            duration,
            job.getErrorMessage() != null ? job.getErrorMessage() : "Unknown error"
        );
        
        message.setText(body);
        
        try {
            mailSender.send(message);
            log.info("Job failure notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send job failure notification", e);
        }
    }

    @Override
    @Async
    public void sendJobSummaryNotification(String jobName, int successCount, int failureCount, int totalRecordsProcessed) {
        if (!emailEnabled) {
            log.debug("Email notifications disabled. Not sending job summary notification.");
            return;
        }
        
        log.info("Sending job summary notification for job: {}", jobName);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(String.format("%s Job Summary: %s", subjectPrefix, jobName));
        
        String body = String.format(
            "ETL Job Summary for %s.\n\n" +
            "Summary:\n" +
            "- Successful Jobs: %d\n" +
            "- Failed Jobs: %d\n" +
            "- Total Jobs: %d\n" +
            "- Success Rate: %.2f%%\n" +
            "- Total Records Processed: %d\n\n" +
            "This is an automated notification from the Stripe-Lunar ETL system.",
            jobName,
            successCount,
            failureCount,
            successCount + failureCount,
            (successCount + failureCount > 0) ? (successCount * 100.0 / (successCount + failureCount)) : 0.0,
            totalRecordsProcessed
        );
        
        message.setText(body);
        
        try {
            mailSender.send(message);
            log.info("Job summary notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send job summary notification", e);
        }
    }
    
    /**
     * Format duration in seconds to a human-readable format
     * 
     * @param seconds Duration in seconds
     * @return Formatted duration string
     */
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d hours, %d minutes, %d seconds", hours, minutes, remainingSeconds);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, remainingSeconds);
        } else {
            return String.format("%d seconds", remainingSeconds);
        }
    }
}
