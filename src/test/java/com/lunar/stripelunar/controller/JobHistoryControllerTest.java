package com.lunar.stripelunar.controller;

import com.lunar.stripelunar.model.ETLJobHistory;
import com.lunar.stripelunar.service.ETLJobHistoryService;
import com.lunar.stripelunar.util.TestCsvExportUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobHistoryControllerTest {

    @Mock
    private ETLJobHistoryService etlJobHistoryService;

    private TestCsvExportUtil csvExportUtil = new TestCsvExportUtil();

    @InjectMocks
    private JobHistoryController jobHistoryController;

    private List<ETLJobHistory> mockJobs;
    private Map<String, Object> mockStatistics;
    private LocalDateTime testStartDate;

    @BeforeEach
    void setUp() {
        // Reset the test CSV export util
        csvExportUtil.reset();
        
        // Manually set the csvExportUtil in the controller
        ReflectionTestUtils.setField(jobHistoryController, "csvExportUtil", csvExportUtil);
        // Setup mock job history records
        mockJobs = new ArrayList<>();
        
        ETLJobHistory job1 = new ETLJobHistory();
        job1.setId(1L);
        job1.setJobName("syncCustomers");
        job1.setStartTime(LocalDateTime.now().minusDays(1));
        job1.setEndTime(LocalDateTime.now().minusDays(1).plusMinutes(5));
        job1.setStatus("COMPLETED");
        job1.setRecordsProcessed(100);
        mockJobs.add(job1);
        
        ETLJobHistory job2 = new ETLJobHistory();
        job2.setId(2L);
        job2.setJobName("syncPayments");
        job2.setStartTime(LocalDateTime.now().minusDays(1));
        job2.setEndTime(LocalDateTime.now().minusDays(1).plusMinutes(7));
        job2.setStatus("COMPLETED");
        job2.setRecordsProcessed(250);
        mockJobs.add(job2);
        
        ETLJobHistory job3 = new ETLJobHistory();
        job3.setId(3L);
        job3.setJobName("syncAll");
        job3.setStartTime(LocalDateTime.now().minusDays(7));
        job3.setEndTime(null);
        job3.setStatus("FAILED");
        job3.setErrorMessage("Connection timeout");
        mockJobs.add(job3);
        
        // Setup mock statistics
        mockStatistics = new HashMap<>();
        mockStatistics.put("totalJobs", 3);
        mockStatistics.put("completedJobs", 2);
        mockStatistics.put("failedJobs", 1);
        
        Map<String, Object> jobTypeStats = new HashMap<>();
        jobTypeStats.put("syncCustomers", 1);
        jobTypeStats.put("syncPayments", 1);
        jobTypeStats.put("syncAll", 1);
        mockStatistics.put("jobsByType", jobTypeStats);
        
        // Test date
        testStartDate = LocalDateTime.now().minusDays(10);
    }

    @Test
    void getJobExecutions_ShouldReturnJobsForSpecificJobName() {
        // Arrange
        String jobName = "syncCustomers";
        List<ETLJobHistory> expectedJobs = Collections.singletonList(mockJobs.get(0));
        when(etlJobHistoryService.getJobExecutions(jobName)).thenReturn(expectedJobs);

        // Act
        ResponseEntity<List<ETLJobHistory>> response = jobHistoryController.getJobExecutions(jobName);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("syncCustomers", response.getBody().get(0).getJobName());
        
        verify(etlJobHistoryService, times(1)).getJobExecutions(jobName);
    }

    @Test
    void getRecentJobs_ShouldReturnJobsAfterStartDate() {
        // Arrange
        when(etlJobHistoryService.getJobsAfterDate(testStartDate)).thenReturn(mockJobs);

        // Act
        ResponseEntity<List<ETLJobHistory>> response = jobHistoryController.getRecentJobs(testStartDate);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        
        verify(etlJobHistoryService, times(1)).getJobsAfterDate(testStartDate);
    }

    @Test
    void getLastJobExecution_WhenJobExists_ShouldReturnJob() {
        // Arrange
        String jobName = "syncCustomers";
        ETLJobHistory expectedJob = mockJobs.get(0);
        when(etlJobHistoryService.getLastJobExecution(jobName)).thenReturn(Optional.of(expectedJob));

        // Act
        ResponseEntity<?> response = jobHistoryController.getLastJobExecution(jobName);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedJob, response.getBody());
        
        verify(etlJobHistoryService, times(1)).getLastJobExecution(jobName);
    }

    @Test
    void getLastJobExecution_WhenJobDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        String jobName = "nonExistentJob";
        when(etlJobHistoryService.getLastJobExecution(jobName)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = jobHistoryController.getLastJobExecution(jobName);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(etlJobHistoryService, times(1)).getLastJobExecution(jobName);
    }

    @Test
    void getJobStatistics_ShouldReturnStatistics() {
        // Arrange
        when(etlJobHistoryService.getJobStatistics()).thenReturn(mockStatistics);

        // Act
        ResponseEntity<Map<String, Object>> response = jobHistoryController.getJobStatistics();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().get("totalJobs"));
        assertEquals(2, response.getBody().get("completedJobs"));
        assertEquals(1, response.getBody().get("failedJobs"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> jobsByType = (Map<String, Object>) response.getBody().get("jobsByType");
        assertNotNull(jobsByType);
        assertEquals(3, jobsByType.size());
        
        verify(etlJobHistoryService, times(1)).getJobStatistics();
    }

    @Test
    void exportJobHistory_WithJobName_ShouldExportJobsForSpecificJobName() throws IOException {
        // Arrange
        String jobName = "syncCustomers";
        List<ETLJobHistory> expectedJobs = Collections.singletonList(mockJobs.get(0));
        when(etlJobHistoryService.getJobExecutions(jobName)).thenReturn(expectedJobs);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // Act
        jobHistoryController.exportJobHistory(jobName, null, response);
        
        // Assert
        assertEquals("text/csv", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains("attachment"));
        assertTrue(response.getHeader("Content-Disposition").contains(".csv"));
        
        verify(etlJobHistoryService, times(1)).getJobExecutions(jobName);
        assertTrue(csvExportUtil.wasExportCalled());
        assertEquals(expectedJobs, csvExportUtil.getLastExportedJobs());
    }

    @Test
    void exportJobHistory_WithStartDate_ShouldExportJobsAfterStartDate() throws IOException {
        // Arrange
        when(etlJobHistoryService.getJobsAfterDate(testStartDate)).thenReturn(mockJobs);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // Act
        jobHistoryController.exportJobHistory(null, testStartDate, response);
        
        // Assert
        assertEquals("text/csv", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains("attachment"));
        assertTrue(response.getHeader("Content-Disposition").contains(".csv"));
        
        verify(etlJobHistoryService, times(1)).getJobsAfterDate(testStartDate);
        assertTrue(csvExportUtil.wasExportCalled());
        assertEquals(mockJobs, csvExportUtil.getLastExportedJobs());
    }

    @Test
    void exportJobHistory_WithNoParameters_ShouldExportLast30DaysJobs() throws IOException {
        // Arrange
        when(etlJobHistoryService.getJobsAfterDate(any(LocalDateTime.class))).thenReturn(mockJobs);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        // Act
        jobHistoryController.exportJobHistory(null, null, response);
        
        // Assert
        assertEquals("text/csv", response.getContentType());
        assertTrue(response.getHeader("Content-Disposition").contains("attachment"));
        assertTrue(response.getHeader("Content-Disposition").contains(".csv"));
        
        verify(etlJobHistoryService, times(1)).getJobsAfterDate(any(LocalDateTime.class));
        assertTrue(csvExportUtil.wasExportCalled());
        assertEquals(mockJobs, csvExportUtil.getLastExportedJobs());
    }
}
