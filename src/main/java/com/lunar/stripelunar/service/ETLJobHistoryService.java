package com.lunar.stripelunar.service;

import com.lunar.stripelunar.model.ETLJobHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ETLJobHistoryService {
    
    /**
     * Start a new ETL job and record it in the history
     * 
     * @param jobName Name of the ETL job
     * @return The created ETL job history record
     */
    ETLJobHistory startJob(String jobName);
    
    /**
     * Complete an ETL job successfully
     * 
     * @param jobId ID of the ETL job
     * @param recordsProcessed Number of records processed
     * @return The updated ETL job history record
     */
    ETLJobHistory completeJob(Long jobId, Integer recordsProcessed);
    
    /**
     * Mark an ETL job as failed
     * 
     * @param jobId ID of the ETL job
     * @param errorMessage Error message describing the failure
     * @return The updated ETL job history record
     */
    ETLJobHistory failJob(Long jobId, String errorMessage);
    
    /**
     * Get the most recent execution of a specific job
     * 
     * @param jobName Name of the ETL job
     * @return Optional containing the most recent job history record, if any
     */
    Optional<ETLJobHistory> getLastJobExecution(String jobName);
    
    /**
     * Get all executions of a specific job
     * 
     * @param jobName Name of the ETL job
     * @return List of job history records for the specified job
     */
    List<ETLJobHistory> getJobExecutions(String jobName);
    
    /**
     * Get all job executions after a specific date
     * 
     * @param startDate Start date for the query
     * @return List of job history records after the specified date
     */
    List<ETLJobHistory> getJobsAfterDate(LocalDateTime startDate);
    
    /**
     * Get job execution statistics
     * 
     * @return Map containing statistics about job executions
     */
    java.util.Map<String, Object> getJobStatistics();
}
