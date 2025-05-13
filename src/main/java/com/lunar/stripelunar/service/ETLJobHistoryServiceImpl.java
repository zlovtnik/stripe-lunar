package com.lunar.stripelunar.service;

import com.lunar.stripelunar.exception.ResourceNotFoundException;
import com.lunar.stripelunar.model.ETLJobHistory;
import com.lunar.stripelunar.notification.NotificationService;
import com.lunar.stripelunar.repository.ETLJobHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ETLJobHistoryServiceImpl implements ETLJobHistoryService {

    private final ETLJobHistoryRepository etlJobHistoryRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ETLJobHistory startJob(String jobName) {
        log.info("Starting ETL job: {}", jobName);
        
        ETLJobHistory job = ETLJobHistory.builder()
                .jobName(jobName)
                .startTime(LocalDateTime.now())
                .status(ETLJobHistory.STATUS_RUNNING)
                .build();
        
        return etlJobHistoryRepository.save(job);
    }

    @Override
    @Transactional
    public ETLJobHistory completeJob(Long jobId, Integer recordsProcessed) {
        log.info("Completing ETL job with ID: {}, records processed: {}", jobId, recordsProcessed);
        
        ETLJobHistory job = etlJobHistoryRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("ETLJobHistory", "id", jobId));
        
        job.setEndTime(LocalDateTime.now());
        job.setStatus(ETLJobHistory.STATUS_COMPLETED);
        job.setRecordsProcessed(recordsProcessed);
        
        ETLJobHistory savedJob = etlJobHistoryRepository.save(job);
        
        // Send notification about job completion
        notificationService.sendJobCompletionNotification(savedJob);
        
        return savedJob;
    }

    @Override
    @Transactional
    public ETLJobHistory failJob(Long jobId, String errorMessage) {
        log.error("ETL job with ID: {} failed with error: {}", jobId, errorMessage);
        
        ETLJobHistory job = etlJobHistoryRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("ETLJobHistory", "id", jobId));
        
        job.setEndTime(LocalDateTime.now());
        job.setStatus(ETLJobHistory.STATUS_FAILED);
        job.setErrorMessage(errorMessage);
        
        ETLJobHistory savedJob = etlJobHistoryRepository.save(job);
        
        // Send notification about job failure
        notificationService.sendJobFailureNotification(savedJob);
        
        return savedJob;
    }

    @Override
    public Optional<ETLJobHistory> getLastJobExecution(String jobName) {
        return etlJobHistoryRepository.findTopByJobNameOrderByStartTimeDesc(jobName);
    }

    @Override
    public List<ETLJobHistory> getJobExecutions(String jobName) {
        return etlJobHistoryRepository.findByJobNameOrderByStartTimeDesc(jobName);
    }

    @Override
    public List<ETLJobHistory> getJobsAfterDate(LocalDateTime startDate) {
        return etlJobHistoryRepository.findJobsAfterDate(startDate);
    }

    @Override
    public Map<String, Object> getJobStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Count total jobs
        long totalJobs = etlJobHistoryRepository.count();
        statistics.put("totalJobs", totalJobs);
        
        // Count jobs by status
        long completedJobs = etlJobHistoryRepository.countByJobNameAndStatus("syncCustomers", ETLJobHistory.STATUS_COMPLETED) +
                             etlJobHistoryRepository.countByJobNameAndStatus("syncPayments", ETLJobHistory.STATUS_COMPLETED) +
                             etlJobHistoryRepository.countByJobNameAndStatus("syncAll", ETLJobHistory.STATUS_COMPLETED);
        
        long failedJobs = etlJobHistoryRepository.countByJobNameAndStatus("syncCustomers", ETLJobHistory.STATUS_FAILED) +
                          etlJobHistoryRepository.countByJobNameAndStatus("syncPayments", ETLJobHistory.STATUS_FAILED) +
                          etlJobHistoryRepository.countByJobNameAndStatus("syncAll", ETLJobHistory.STATUS_FAILED);
        
        long runningJobs = etlJobHistoryRepository.countByJobNameAndStatus("syncCustomers", ETLJobHistory.STATUS_RUNNING) +
                           etlJobHistoryRepository.countByJobNameAndStatus("syncPayments", ETLJobHistory.STATUS_RUNNING) +
                           etlJobHistoryRepository.countByJobNameAndStatus("syncAll", ETLJobHistory.STATUS_RUNNING);
        
        statistics.put("completedJobs", completedJobs);
        statistics.put("failedJobs", failedJobs);
        statistics.put("runningJobs", runningJobs);
        
        // Get last execution times for each job type
        Optional<ETLJobHistory> lastCustomerSync = getLastJobExecution("syncCustomers");
        Optional<ETLJobHistory> lastPaymentSync = getLastJobExecution("syncPayments");
        Optional<ETLJobHistory> lastFullSync = getLastJobExecution("syncAll");
        
        Map<String, Object> lastExecutions = new HashMap<>();
        lastCustomerSync.ifPresent(job -> lastExecutions.put("syncCustomers", formatJobInfo(job)));
        lastPaymentSync.ifPresent(job -> lastExecutions.put("syncPayments", formatJobInfo(job)));
        lastFullSync.ifPresent(job -> lastExecutions.put("syncAll", formatJobInfo(job)));
        
        statistics.put("lastExecutions", lastExecutions);
        
        return statistics;
    }
    
    private Map<String, Object> formatJobInfo(ETLJobHistory job) {
        Map<String, Object> jobInfo = new HashMap<>();
        jobInfo.put("id", job.getId());
        jobInfo.put("startTime", job.getStartTime());
        jobInfo.put("endTime", job.getEndTime());
        jobInfo.put("status", job.getStatus());
        jobInfo.put("recordsProcessed", job.getRecordsProcessed());
        
        if (job.getEndTime() != null && job.getStartTime() != null) {
            long durationSeconds = java.time.Duration.between(job.getStartTime(), job.getEndTime()).getSeconds();
            jobInfo.put("durationSeconds", durationSeconds);
        }
        
        return jobInfo;
    }
}
