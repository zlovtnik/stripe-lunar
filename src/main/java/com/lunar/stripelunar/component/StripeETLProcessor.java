package com.lunar.stripelunar.component;

import com.lunar.stripelunar.model.Customer;
import com.lunar.stripelunar.model.ETLJobHistory;
import com.lunar.stripelunar.model.Payment;
import com.lunar.stripelunar.service.ETLJobHistoryService;
import com.lunar.stripelunar.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripeETLProcessor implements Processor {

    private final StripeService stripeService;
    private final ETLJobHistoryService etlJobHistoryService;

    @Override
    public void process(Exchange exchange) throws Exception {
        String operation = exchange.getIn().getHeader("operation", String.class);
        
        if (operation == null) {
            throw new IllegalArgumentException("Operation header is required");
        }
        
        log.info("Processing ETL operation: {}", operation);
        ETLJobHistory job = null;
        
        try {
            // Start job tracking
            job = etlJobHistoryService.startJob(operation);
            exchange.setProperty("etlJobId", job.getId());
            
            switch (operation) {
                case "syncCustomers":
                    List<Customer> customers = stripeService.syncCustomers();
                    exchange.getMessage().setBody(customers);
                    // Complete job tracking
                    etlJobHistoryService.completeJob(job.getId(), customers.size());
                    break;
                    
                case "syncPayments":
                    List<Payment> payments = stripeService.syncPayments();
                    exchange.getMessage().setBody(payments);
                    // Complete job tracking
                    etlJobHistoryService.completeJob(job.getId(), payments.size());
                    break;
                    
                case "syncAll":
                    Map<String, Object> result = new HashMap<>();
                    List<Customer> syncedCustomers = stripeService.syncCustomers();
                    List<Payment> syncedPayments = stripeService.syncPayments();
                    
                    result.put("customers", syncedCustomers);
                    result.put("payments", syncedPayments);
                    result.put("customersCount", syncedCustomers.size());
                    result.put("paymentsCount", syncedPayments.size());
                    
                    exchange.getMessage().setBody(result);
                    // Complete job tracking
                    etlJobHistoryService.completeJob(job.getId(), syncedCustomers.size() + syncedPayments.size());
                    break;
                    
                case "status":
                    Map<String, Object> status = new HashMap<>();
                    status.put("customersCount", stripeService.getAllCustomers().size());
                    status.put("paymentsCount", stripeService.getAllPayments().size());
                    status.put("jobStatistics", etlJobHistoryService.getJobStatistics());
                    
                    exchange.getMessage().setBody(status);
                    // Complete job tracking (status check is not a real ETL job)
                    etlJobHistoryService.completeJob(job.getId(), 0);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unknown operation: " + operation);
            }
        } catch (Exception e) {
            // Record job failure
            if (job != null) {
                etlJobHistoryService.failJob(job.getId(), e.getMessage());
            }
            throw e; // Rethrow to let the error handler deal with it
        }
    }
}
