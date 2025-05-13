package com.lunar.stripelunar.util;

import com.lunar.stripelunar.model.ETLJobHistory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CsvExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Export ETL job history data to CSV format
     * 
     * @param jobs List of ETL job history records to export
     * @param writer Writer to output the CSV data
     * @throws IOException If an I/O error occurs
     */
    public void exportJobHistoryToCsv(List<ETLJobHistory> jobs, Writer writer) throws IOException {
        String[] headers = {
            "Job ID", "Job Name", "Start Time", "End Time", "Status", 
            "Records Processed", "Duration (seconds)", "Error Message"
        };
        
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(headers).build())) {
            for (ETLJobHistory job : jobs) {
                Long durationSeconds = null;
                if (job.getEndTime() != null && job.getStartTime() != null) {
                    durationSeconds = java.time.Duration.between(job.getStartTime(), job.getEndTime()).getSeconds();
                }
                
                csvPrinter.printRecord(
                    job.getId(),
                    job.getJobName(),
                    job.getStartTime() != null ? job.getStartTime().format(DATE_FORMATTER) : "",
                    job.getEndTime() != null ? job.getEndTime().format(DATE_FORMATTER) : "",
                    job.getStatus(),
                    job.getRecordsProcessed(),
                    durationSeconds,
                    job.getErrorMessage()
                );
            }
            
            csvPrinter.flush();
        }
    }
}
