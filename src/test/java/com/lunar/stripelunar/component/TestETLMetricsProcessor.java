package com.lunar.stripelunar.component;

import org.apache.camel.Exchange;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test-specific implementation of ETLMetricsProcessor for testing purposes.
 * This implementation avoids the issues with mocking final classes in Java 23.
 */
public class TestETLMetricsProcessor extends ETLMetricsProcessor {
    
    private final Map<String, ETLOperationMetrics> testMetricsMap = new ConcurrentHashMap<>();
    
    /**
     * Reset all metrics for testing purposes
     */
    public void reset() {
        testMetricsMap.clear();
    }
    
    /**
     * Add a test metric for a specific operation
     */
    public void addTestMetric(String operation, long count, LocalDateTime lastExecutionTime) {
        ETLOperationMetrics metrics = new ETLOperationMetrics();
        for (int i = 0; i < count; i++) {
            metrics.incrementCount();
        }
        metrics.setLastExecutionTime(lastExecutionTime);
        testMetricsMap.put(operation, metrics);
    }
    
    @Override
    public void process(Exchange exchange) throws Exception {
        String operation = exchange.getIn().getHeader("operation", String.class);
        
        if (operation == null) {
            return;
        }
        
        ETLOperationMetrics metrics = testMetricsMap.computeIfAbsent(operation, k -> new ETLOperationMetrics());
        metrics.incrementCount();
        metrics.setLastExecutionTime(LocalDateTime.now());
        
        // Add metrics to exchange for potential use downstream
        Map<String, Object> metricsData = new HashMap<>();
        metricsData.put("operation", operation);
        metricsData.put("executionCount", metrics.getExecutionCount());
        metricsData.put("lastExecutionTime", metrics.getLastExecutionTime());
        
        exchange.setProperty("etlMetrics", metricsData);
    }
    
    @Override
    public Map<String, Object> getAllMetrics() {
        Map<String, Object> result = new HashMap<>();
        
        testMetricsMap.forEach((operation, metrics) -> {
            Map<String, Object> operationMetrics = new HashMap<>();
            operationMetrics.put("executionCount", metrics.getExecutionCount());
            operationMetrics.put("lastExecutionTime", metrics.getLastExecutionTime());
            result.put(operation, operationMetrics);
        });
        
        return result;
    }
}
