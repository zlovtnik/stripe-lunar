package com.lunar.stripelunar.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Access(AccessType.FIELD)
@DynamicUpdate
@DynamicInsert
@NamedQuery(name = "ETLJobHistory.findAll", query = "SELECT e FROM ETLJobHistory e")
@NamedQuery(name = "ETLJobHistory.findById", query = "SELECT e FROM ETLJobHistory e WHERE e.id = :id")
@NamedQuery(name = "ETLJobHistory.findByJobName", query = "SELECT e FROM ETLJobHistory e WHERE e.jobName = :jobName")
@NamedQuery(name = "ETLJobHistory.findByStatus", query = "SELECT e FROM ETLJobHistory e WHERE e.status = :status")
@Table(name = "ETL_JOB_HISTORY")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ETLJobHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "JOB_ID")
    private Long id;

    @Column(name = "JOB_NAME", nullable = false)
    private String jobName;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "RECORDS_PROCESSED")
    private Integer recordsProcessed;

    @Column(name = "ERROR_MESSAGE", length = 4000)
    private String errorMessage;
    
    // Helper methods for job status
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    
    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }
    
    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }
    
    public boolean isRunning() {
        return STATUS_RUNNING.equals(status);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }
    
    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public static ETLJobHistoryBuilder builder() {
        return new ETLJobHistoryBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}

