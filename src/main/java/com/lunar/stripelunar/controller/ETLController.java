package com.lunar.stripelunar.controller;

import com.lunar.stripelunar.component.ETLMetricsProcessor;
import com.lunar.stripelunar.model.Customer;
import com.lunar.stripelunar.model.Payment;
import com.lunar.stripelunar.service.StripeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/etl")
@RequiredArgsConstructor
@Slf4j
public class ETLController {

    private final StripeService stripeService;
    private final ETLMetricsProcessor etlMetricsProcessor;

    @GetMapping("/sync/all")
    public ResponseEntity<Map<String, Object>> syncAll() {
        log.info("Manual sync of all Stripe data initiated");
        
        List<Customer> customers = stripeService.syncCustomers();
        List<Payment> payments = stripeService.syncPayments();
        
        Map<String, Object> result = new HashMap<>();
        result.put("customersCount", customers.size());
        result.put("paymentsCount", payments.size());
        result.put("status", "completed");
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("ETL status check requested");
        
        long customerCount = stripeService.getAllCustomers().size();
        long paymentCount = stripeService.getAllPayments().size();
        
        // Get metrics from the ETL metrics processor
        Map<String, Object> metrics = etlMetricsProcessor.getAllMetrics();
        
        Map<String, Object> status = new HashMap<>();
        status.put("customersCount", customerCount);
        status.put("paymentsCount", paymentCount);
        status.put("metrics", metrics);
        
        // Determine last sync times from metrics
        Map<String, Object> lastSyncTimes = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        if (metrics.containsKey("syncCustomers")) {
            Map<String, Object> customerMetrics = (Map<String, Object>) metrics.get("syncCustomers");
            if (customerMetrics.containsKey("lastExecutionTime")) {
                LocalDateTime lastSync = (LocalDateTime) customerMetrics.get("lastExecutionTime");
                lastSyncTimes.put("customers", lastSync != null ? lastSync.format(formatter) : "Never");
            }
        } else {
            lastSyncTimes.put("customers", "Never");
        }
        
        if (metrics.containsKey("syncPayments")) {
            Map<String, Object> paymentMetrics = (Map<String, Object>) metrics.get("syncPayments");
            if (paymentMetrics.containsKey("lastExecutionTime")) {
                LocalDateTime lastSync = (LocalDateTime) paymentMetrics.get("lastExecutionTime");
                lastSyncTimes.put("payments", lastSync != null ? lastSync.format(formatter) : "Never");
            }
        } else {
            lastSyncTimes.put("payments", "Never");
        }
        
        if (metrics.containsKey("syncAll")) {
            Map<String, Object> allMetrics = (Map<String, Object>) metrics.get("syncAll");
            if (allMetrics.containsKey("lastExecutionTime")) {
                LocalDateTime lastSync = (LocalDateTime) allMetrics.get("lastExecutionTime");
                lastSyncTimes.put("fullSync", lastSync != null ? lastSync.format(formatter) : "Never");
            }
        } else {
            lastSyncTimes.put("fullSync", "Never");
        }
        
        status.put("lastSyncTimes", lastSyncTimes);
        status.put("applicationStatus", "healthy");
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        log.info("ETL metrics requested");
        
        Map<String, Object> metrics = etlMetricsProcessor.getAllMetrics();
        Map<String, Object> response = new HashMap<>();
        response.put("metrics", metrics);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return ResponseEntity.ok(response);
    }
}
