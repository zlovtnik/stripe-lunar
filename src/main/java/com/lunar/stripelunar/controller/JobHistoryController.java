package com.lunar.stripelunar.controller;

import com.lunar.stripelunar.model.ETLJobHistory;
import com.lunar.stripelunar.service.ETLJobHistoryService;
import com.lunar.stripelunar.util.CsvExportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Job History", description = "ETL Job History Management API")
public class JobHistoryController {

    private final ETLJobHistoryService etlJobHistoryService;
    private final CsvExportUtil csvExportUtil;

    @GetMapping
    @Operation(summary = "Get all job executions for a specific job type")
    public ResponseEntity<List<ETLJobHistory>> getJobExecutions(
            @Parameter(description = "Job name to filter by", required = true)
            @RequestParam String jobName) {
        log.info("Retrieving job executions for job: {}", jobName);
        List<ETLJobHistory> jobs = etlJobHistoryService.getJobExecutions(jobName);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/recent")
    @Operation(summary = "Get jobs executed after a specific date")
    public ResponseEntity<List<ETLJobHistory>> getRecentJobs(
            @Parameter(description = "Start date for job filtering (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {
        log.info("Retrieving jobs executed after: {}", startDate);
        List<ETLJobHistory> jobs = etlJobHistoryService.getJobsAfterDate(startDate);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/last")
    @Operation(summary = "Get the most recent execution of a specific job")
    public ResponseEntity<?> getLastJobExecution(
            @Parameter(description = "Job name to retrieve", required = true)
            @RequestParam String jobName) {
        log.info("Retrieving last execution for job: {}", jobName);
        return etlJobHistoryService.getLastJobExecution(jobName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get job execution statistics")
    public ResponseEntity<Map<String, Object>> getJobStatistics() {
        log.info("Retrieving job statistics");
        Map<String, Object> statistics = etlJobHistoryService.getJobStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/export")
    @Operation(summary = "Export job history to CSV")
    public void exportJobHistory(
            @Parameter(description = "Job name to filter by (optional)")
            @RequestParam(required = false) String jobName,
            @Parameter(description = "Start date for job filtering (ISO format, optional)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            HttpServletResponse response) throws IOException {
        
        log.info("Exporting job history to CSV. Job name: {}, Start date: {}", jobName, startDate);
        
        // Set response headers
        String filename = "etl-job-history-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".csv";
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        
        // Get job history data
        List<ETLJobHistory> jobs;
        if (jobName != null && !jobName.isEmpty()) {
            jobs = etlJobHistoryService.getJobExecutions(jobName);
        } else if (startDate != null) {
            jobs = etlJobHistoryService.getJobsAfterDate(startDate);
        } else {
            // Get all jobs (limited to last 100 for performance)
            jobs = etlJobHistoryService.getJobsAfterDate(LocalDateTime.now().minusDays(30));
        }
        
        // Export to CSV
        csvExportUtil.exportJobHistoryToCsv(jobs, response.getWriter());
    }
}
