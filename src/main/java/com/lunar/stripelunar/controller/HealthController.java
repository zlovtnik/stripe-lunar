package com.lunar.stripelunar.controller;

import com.lunar.stripelunar.component.ETLMetricsProcessor;
import com.lunar.stripelunar.repository.CustomerRepository;
import com.lunar.stripelunar.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final ETLMetricsProcessor etlMetricsProcessor;
    
    private final LocalDateTime applicationStartTime = LocalDateTime.now();

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        // Application uptime
        Map<String, Object> application = new HashMap<>();
        application.put("startTime", applicationStartTime.format(DateTimeFormatter.ISO_DATE_TIME));
        application.put("uptime", getUptimeInSeconds());
        health.put("application", application);
        
        // Database status
        Map<String, Object> database = new HashMap<>();
        try {
            long customerCount = customerRepository.count();
            long paymentCount = paymentRepository.count();
            database.put("status", "UP");
            database.put("customerCount", customerCount);
            database.put("paymentCount", paymentCount);
        } catch (Exception e) {
            database.put("status", "DOWN");
            database.put("error", e.getMessage());
        }
        health.put("database", database);
        
        // ETL metrics summary
        Map<String, Object> etlMetrics = new HashMap<>();
        try {
            Map<String, Object> metrics = etlMetricsProcessor.getAllMetrics();
            etlMetrics.put("status", "UP");
            etlMetrics.put("operationsCount", metrics.size());
            etlMetrics.put("details", metrics);
        } catch (Exception e) {
            etlMetrics.put("status", "DOWN");
            etlMetrics.put("error", e.getMessage());
        }
        health.put("etl", etlMetrics);
        
        return ResponseEntity.ok(health);
    }
    
    private long getUptimeInSeconds() {
        return java.time.Duration.between(applicationStartTime, LocalDateTime.now()).getSeconds();
    }
}
