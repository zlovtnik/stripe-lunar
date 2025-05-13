package com.lunar.stripelunar.service;

import com.lunar.stripelunar.exception.ResourceNotFoundException;
import com.lunar.stripelunar.model.ETLJobHistory;
import com.lunar.stripelunar.notification.NotificationService;
import com.lunar.stripelunar.repository.ETLJobHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ETLJobHistoryServiceTest {

    @Mock
    private ETLJobHistoryRepository etlJobHistoryRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ETLJobHistoryServiceImpl etlJobHistoryService;

    @Captor
    private ArgumentCaptor<ETLJobHistory> jobHistoryCaptor;

    private ETLJobHistory mockRunningJob;
    private ETLJobHistory mockCompletedJob;
    private ETLJobHistory mockFailedJob;
    private LocalDateTime testStartDate;

    @BeforeEach
    void setUp() {
        // Setup test date
        testStartDate = LocalDateTime.now().minusDays(10);
        
        // Setup mock running job
        mockRunningJob = ETLJobHistory.builder()
                .id(1L)
                .jobName("syncCustomers")
                .startTime(LocalDateTime.now().minusMinutes(5))
                .status(ETLJobHistory.STATUS_RUNNING)
                .build();
        
        // Setup mock completed job
        mockCompletedJob = ETLJobHistory.builder()
                .id(2L)
                .jobName("syncPayments")
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().minusHours(1).plusMinutes(5))
                .status(ETLJobHistory.STATUS_COMPLETED)
                .recordsProcessed(100)
                .build();
        
        // Setup mock failed job
        mockFailedJob = ETLJobHistory.builder()
                .id(3L)
                .jobName("syncAll")
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().minusDays(1).plusMinutes(2))
                .status(ETLJobHistory.STATUS_FAILED)
                .errorMessage("Connection timeout")
                .build();
    }

    @Test
    void startJob_ShouldCreateNewJobWithRunningStatus() {
        // Arrange
        String jobName = "syncCustomers";
        when(etlJobHistoryRepository.save(any(ETLJobHistory.class))).thenReturn(mockRunningJob);

        // Act
        ETLJobHistory result = etlJobHistoryService.startJob(jobName);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals(jobName, result.getJobName());
        assertEquals(ETLJobHistory.STATUS_RUNNING, result.getStatus());
        assertNotNull(result.getStartTime());
        
        verify(etlJobHistoryRepository).save(jobHistoryCaptor.capture());
        ETLJobHistory capturedJob = jobHistoryCaptor.getValue();
        assertEquals(jobName, capturedJob.getJobName());
        assertEquals(ETLJobHistory.STATUS_RUNNING, capturedJob.getStatus());
        assertNotNull(capturedJob.getStartTime());
    }

    @Test
    void completeJob_WhenJobExists_ShouldUpdateJobToCompleted() {
        // Arrange
        Long jobId = 1L;
        Integer recordsProcessed = 100;
        
        when(etlJobHistoryRepository.findById(jobId)).thenReturn(Optional.of(mockRunningJob));
        when(etlJobHistoryRepository.save(any(ETLJobHistory.class))).thenReturn(mockCompletedJob);

        // Act
        ETLJobHistory result = etlJobHistoryService.completeJob(jobId, recordsProcessed);

        // Assert
        assertEquals(ETLJobHistory.STATUS_COMPLETED, result.getStatus());
        assertEquals(recordsProcessed, result.getRecordsProcessed());
        assertNotNull(result.getEndTime());
        
        verify(etlJobHistoryRepository).findById(jobId);
        verify(etlJobHistoryRepository).save(jobHistoryCaptor.capture());
        verify(notificationService).sendJobCompletionNotification(any(ETLJobHistory.class));
        
        ETLJobHistory capturedJob = jobHistoryCaptor.getValue();
        assertEquals(ETLJobHistory.STATUS_COMPLETED, capturedJob.getStatus());
        assertEquals(recordsProcessed, capturedJob.getRecordsProcessed());
        assertNotNull(capturedJob.getEndTime());
    }

    @Test
    void completeJob_WhenJobDoesNotExist_ShouldThrowException() {
        // Arrange
        Long jobId = 999L;
        Integer recordsProcessed = 100;
        
        when(etlJobHistoryRepository.findById(jobId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> etlJobHistoryService.completeJob(jobId, recordsProcessed));
        
        verify(etlJobHistoryRepository).findById(jobId);
        verify(etlJobHistoryRepository, never()).save(any(ETLJobHistory.class));
        verify(notificationService, never()).sendJobCompletionNotification(any(ETLJobHistory.class));
    }

    @Test
    void failJob_WhenJobExists_ShouldUpdateJobToFailed() {
        // Arrange
        Long jobId = 1L;
        String errorMessage = "Connection timeout";
        
        when(etlJobHistoryRepository.findById(jobId)).thenReturn(Optional.of(mockRunningJob));
        when(etlJobHistoryRepository.save(any(ETLJobHistory.class))).thenReturn(mockFailedJob);

        // Act
        ETLJobHistory result = etlJobHistoryService.failJob(jobId, errorMessage);

        // Assert
        assertEquals(ETLJobHistory.STATUS_FAILED, result.getStatus());
        assertEquals(errorMessage, result.getErrorMessage());
        assertNotNull(result.getEndTime());
        
        verify(etlJobHistoryRepository).findById(jobId);
        verify(etlJobHistoryRepository).save(jobHistoryCaptor.capture());
        verify(notificationService).sendJobFailureNotification(any(ETLJobHistory.class));
        
        ETLJobHistory capturedJob = jobHistoryCaptor.getValue();
        assertEquals(ETLJobHistory.STATUS_FAILED, capturedJob.getStatus());
        assertEquals(errorMessage, capturedJob.getErrorMessage());
        assertNotNull(capturedJob.getEndTime());
    }

    @Test
    void failJob_WhenJobDoesNotExist_ShouldThrowException() {
        // Arrange
        Long jobId = 999L;
        String errorMessage = "Connection timeout";
        
        when(etlJobHistoryRepository.findById(jobId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> etlJobHistoryService.failJob(jobId, errorMessage));
        
        verify(etlJobHistoryRepository).findById(jobId);
        verify(etlJobHistoryRepository, never()).save(any(ETLJobHistory.class));
        verify(notificationService, never()).sendJobFailureNotification(any(ETLJobHistory.class));
    }

    @Test
    void getLastJobExecution_ShouldReturnLastJob() {
        // Arrange
        String jobName = "syncCustomers";
        when(etlJobHistoryRepository.findTopByJobNameOrderByStartTimeDesc(jobName))
                .thenReturn(Optional.of(mockRunningJob));

        // Act
        Optional<ETLJobHistory> result = etlJobHistoryService.getLastJobExecution(jobName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockRunningJob, result.get());
        
        verify(etlJobHistoryRepository).findTopByJobNameOrderByStartTimeDesc(jobName);
    }

    @Test
    void getJobExecutions_ShouldReturnAllJobsForName() {
        // Arrange
        String jobName = "syncCustomers";
        List<ETLJobHistory> expectedJobs = Collections.singletonList(mockRunningJob);
        when(etlJobHistoryRepository.findByJobNameOrderByStartTimeDesc(jobName))
                .thenReturn(expectedJobs);

        // Act
        List<ETLJobHistory> result = etlJobHistoryService.getJobExecutions(jobName);

        // Assert
        assertEquals(expectedJobs.size(), result.size());
        assertEquals(expectedJobs.get(0), result.get(0));
        
        verify(etlJobHistoryRepository).findByJobNameOrderByStartTimeDesc(jobName);
    }

    @Test
    void getJobsAfterDate_ShouldReturnJobsAfterDate() {
        // Arrange
        List<ETLJobHistory> expectedJobs = Arrays.asList(mockRunningJob, mockCompletedJob, mockFailedJob);
        when(etlJobHistoryRepository.findJobsAfterDate(testStartDate))
                .thenReturn(expectedJobs);

        // Act
        List<ETLJobHistory> result = etlJobHistoryService.getJobsAfterDate(testStartDate);

        // Assert
        assertEquals(expectedJobs.size(), result.size());
        
        verify(etlJobHistoryRepository).findJobsAfterDate(testStartDate);
    }

    @Test
    void getJobStatistics_ShouldReturnStatistics() {
        // Arrange
        when(etlJobHistoryRepository.count()).thenReturn(10L);
        
        // Mock counts by job name and status
        when(etlJobHistoryRepository.countByJobNameAndStatus("syncCustomers", ETLJobHistory.STATUS_COMPLETED)).thenReturn(3L);
        when(etlJobHistoryRepository.countByJobNameAndStatus("syncPayments", ETLJobHistory.STATUS_COMPLETED)).thenReturn(2L);
        when(etlJobHistoryRepository.countByJobNameAndStatus("syncAll", ETLJobHistory.STATUS_COMPLETED)).thenReturn(1L);
        
        when(etlJobHistoryRepository.countByJobNameAndStatus("syncCustomers", ETLJobHistory.STATUS_FAILED)).thenReturn(1L);
        when(etlJobHistoryRepository.countByJobNameAndStatus("syncPayments", ETLJobHistory.STATUS_FAILED)).thenReturn(1L);
        when(etlJobHistoryRepository.countByJobNameAndStatus("syncAll", ETLJobHistory.STATUS_FAILED)).thenReturn(1L);
        
        when(etlJobHistoryRepository.countByJobNameAndStatus("syncCustomers", ETLJobHistory.STATUS_RUNNING)).thenReturn(0L);
        when(etlJobHistoryRepository.countByJobNameAndStatus("syncPayments", ETLJobHistory.STATUS_RUNNING)).thenReturn(0L);
        when(etlJobHistoryRepository.countByJobNameAndStatus("syncAll", ETLJobHistory.STATUS_RUNNING)).thenReturn(1L);
        
        // Mock last executions
        when(etlJobHistoryRepository.findTopByJobNameOrderByStartTimeDesc("syncCustomers"))
                .thenReturn(Optional.of(mockCompletedJob));
        when(etlJobHistoryRepository.findTopByJobNameOrderByStartTimeDesc("syncPayments"))
                .thenReturn(Optional.of(mockCompletedJob));
        when(etlJobHistoryRepository.findTopByJobNameOrderByStartTimeDesc("syncAll"))
                .thenReturn(Optional.of(mockFailedJob));

        // Act
        Map<String, Object> result = etlJobHistoryService.getJobStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.get("totalJobs"));
        assertEquals(6L, result.get("completedJobs"));
        assertEquals(3L, result.get("failedJobs"));
        assertEquals(1L, result.get("runningJobs"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> lastExecutions = (Map<String, Object>) result.get("lastExecutions");
        assertNotNull(lastExecutions);
        assertTrue(lastExecutions.containsKey("syncCustomers"));
        assertTrue(lastExecutions.containsKey("syncPayments"));
        assertTrue(lastExecutions.containsKey("syncAll"));
        
        verify(etlJobHistoryRepository).count();
        verify(etlJobHistoryRepository, times(3)).findTopByJobNameOrderByStartTimeDesc(anyString());
    }
}
