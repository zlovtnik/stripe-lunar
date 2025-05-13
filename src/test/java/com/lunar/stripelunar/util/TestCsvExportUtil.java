package com.lunar.stripelunar.util;

import com.lunar.stripelunar.model.ETLJobHistory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

/**
 * Test implementation of CsvExportUtil for testing purposes
 */
@Component
public class TestCsvExportUtil extends CsvExportUtil {
    
    private boolean exportCalled = false;
    private List<ETLJobHistory> lastExportedJobs;
    
    @Override
    public void exportJobHistoryToCsv(List<ETLJobHistory> jobs, Writer writer) throws IOException {
        this.exportCalled = true;
        this.lastExportedJobs = jobs;
        
        PrintWriter printWriter = new PrintWriter(writer);
        
        // Write a simple CSV header for testing
        printWriter.println("ID,Job Name,Status");
        
        // Write a row for each job
        for (ETLJobHistory job : jobs) {
            printWriter.println(job.getId() + "," + job.getJobName() + "," + job.getStatus());
        }
        
        printWriter.flush();
    }
    
    public boolean wasExportCalled() {
        return exportCalled;
    }
    
    public List<ETLJobHistory> getLastExportedJobs() {
        return lastExportedJobs;
    }
    
    public void reset() {
        exportCalled = false;
        lastExportedJobs = null;
    }
}
