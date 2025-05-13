package com.lunar.stripelunar.util;

import com.lunar.stripelunar.model.ETLJobHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CsvExportUtilTest {

    @InjectMocks
    private CsvExportUtil csvExportUtil;

    private List<ETLJobHistory> testJobs;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        testJobs = new ArrayList<>();
        
        // Create a completed job
        ETLJobHistory completedJob = new ETLJobHistory();
        completedJob.setId(1L);
        completedJob.setJobName("syncCustomers");
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = startTime.plusMinutes(5);
        completedJob.setStartTime(startTime);
        completedJob.setEndTime(endTime);
        completedJob.setStatus(ETLJobHistory.STATUS_COMPLETED);
        completedJob.setRecordsProcessed(100);
        testJobs.add(completedJob);
        
        // Create a failed job
        ETLJobHistory failedJob = new ETLJobHistory();
        failedJob.setId(2L);
        failedJob.setJobName("syncPayments");
        startTime = LocalDateTime.now().minusHours(2);
        endTime = startTime.plusMinutes(2);
        failedJob.setStartTime(startTime);
        failedJob.setEndTime(endTime);
        failedJob.setStatus(ETLJobHistory.STATUS_FAILED);
        failedJob.setErrorMessage("Connection timeout");
        testJobs.add(failedJob);
        
        // Create a running job
        ETLJobHistory runningJob = new ETLJobHistory();
        runningJob.setId(3L);
        runningJob.setJobName("syncAll");
        runningJob.setStartTime(LocalDateTime.now().minusMinutes(10));
        runningJob.setStatus(ETLJobHistory.STATUS_RUNNING);
        testJobs.add(runningJob);
    }

    @Test
    void exportJobHistoryToCsv_ShouldGenerateCorrectCSV() throws Exception {
        // Arrange
        StringWriter stringWriter = new StringWriter();
        
        // Act
        csvExportUtil.exportJobHistoryToCsv(testJobs, stringWriter);
        String csvOutput = stringWriter.toString();
        
        // Assert
        assertNotNull(csvOutput);
        assertTrue(csvOutput.length() > 0);
        
        // Check headers
        assertTrue(csvOutput.contains("Job ID,Job Name,Start Time,End Time,Status,Records Processed,Duration (seconds),Error Message"));
        
        // Check completed job data
        ETLJobHistory completedJob = testJobs.get(0);
        assertTrue(csvOutput.contains(completedJob.getId().toString()));
        assertTrue(csvOutput.contains(completedJob.getJobName()));
        assertTrue(csvOutput.contains(completedJob.getStartTime().format(DATE_FORMATTER)));
        assertTrue(csvOutput.contains(completedJob.getEndTime().format(DATE_FORMATTER)));
        assertTrue(csvOutput.contains(completedJob.getStatus()));
        assertTrue(csvOutput.contains(completedJob.getRecordsProcessed().toString()));
        assertTrue(csvOutput.contains("300")); // 5 minutes = 300 seconds
        
        // Check failed job data
        ETLJobHistory failedJob = testJobs.get(1);
        assertTrue(csvOutput.contains(failedJob.getId().toString()));
        assertTrue(csvOutput.contains(failedJob.getJobName()));
        assertTrue(csvOutput.contains(failedJob.getStartTime().format(DATE_FORMATTER)));
        assertTrue(csvOutput.contains(failedJob.getEndTime().format(DATE_FORMATTER)));
        assertTrue(csvOutput.contains(failedJob.getStatus()));
        assertTrue(csvOutput.contains(failedJob.getErrorMessage()));
        assertTrue(csvOutput.contains("120")); // 2 minutes = 120 seconds
        
        // Check running job data
        ETLJobHistory runningJob = testJobs.get(2);
        assertTrue(csvOutput.contains(runningJob.getId().toString()));
        assertTrue(csvOutput.contains(runningJob.getJobName()));
        assertTrue(csvOutput.contains(runningJob.getStartTime().format(DATE_FORMATTER)));
        assertTrue(csvOutput.contains(runningJob.getStatus()));
        
        // Running job should have empty end time and duration
        String[] lines = csvOutput.split("\n");
        assertTrue(lines.length >= 4); // Header + 3 jobs
        String runningJobLine = lines[3]; // 0-based index, so line 3 is the running job
        String[] fields = runningJobLine.split(",");
        assertEquals("", fields[3]); // End time should be empty
        assertEquals("", fields[6]); // Duration should be empty
    }

    @Test
    void exportJobHistoryToCsv_WithEmptyList_ShouldOnlyContainHeaders() throws Exception {
        // Arrange
        StringWriter stringWriter = new StringWriter();
        List<ETLJobHistory> emptyList = new ArrayList<>();
        
        // Act
        csvExportUtil.exportJobHistoryToCsv(emptyList, stringWriter);
        String csvOutput = stringWriter.toString();
        
        // Assert
        assertNotNull(csvOutput);
        assertTrue(csvOutput.length() > 0);
        
        // Check that only headers are present
        String[] lines = csvOutput.split("\n");
        assertEquals(1, lines.length);
        assertTrue(lines[0].contains("Job ID,Job Name,Start Time,End Time,Status,Records Processed,Duration (seconds),Error Message"));
    }
}
