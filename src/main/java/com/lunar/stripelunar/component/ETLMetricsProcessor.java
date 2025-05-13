package com.lunar.stripelunar.component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class ETLMetricsProcessor implements Processor {

    @Getter
    private final Map<String, ETLOperationMetrics> metricsMap = new ConcurrentHashMap<>();

    @Override
    public void process(Exchange exchange) throws Exception {
        String operation = exchange.getIn().getHeader("operation", String.class);
        
        if (operation == null) {
            return;
        }
        
        ETLOperationMetrics metrics = metricsMap.computeIfAbsent(operation, k -> new ETLOperationMetrics());
        metrics.incrementCount();
        metrics.setLastExecutionTime(LocalDateTime.now());
        
        // Add metrics to exchange for potential use downstream
        Map<String, Object> metricsData = new HashMap<>();
        metricsData.put("operation", operation);
        metricsData.put("executionCount", metrics.getExecutionCount());
        metricsData.put("lastExecutionTime", metrics.getLastExecutionTime());
        
        exchange.setProperty("etlMetrics", metricsData);
        
        log.debug("ETL operation '{}' metrics updated: count={}, lastExecution={}", 
                operation, metrics.getExecutionCount(), metrics.getLastExecutionTime());
    }
    
    public Map<String, Object> getAllMetrics() {
        Map<String, Object> result = new HashMap<>();
        
        metricsMap.forEach((operation, metrics) -> {
            Map<String, Object> operationMetrics = new HashMap<>();
            operationMetrics.put("executionCount", metrics.getExecutionCount());
            operationMetrics.put("lastExecutionTime", metrics.getLastExecutionTime());
            result.put(operation, operationMetrics);
        });
        
        return result;
    }
    
    public static class ETLOperationMetrics {
        private final AtomicLong executionCount = new AtomicLong(0);
        private LocalDateTime lastExecutionTime;
        
        public AtomicLong getExecutionCount() {
            return executionCount;
        }
        
        public LocalDateTime getLastExecutionTime() {
            return lastExecutionTime;
        }
        
        public void incrementCount() {
            executionCount.incrementAndGet();
        }
        
        public void setLastExecutionTime(LocalDateTime time) {
            this.lastExecutionTime = time;
        }
    }
}
