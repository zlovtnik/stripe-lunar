package com.lunar.stripelunar.component;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ETLMetricsProcessorTest {

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    private TestETLMetricsProcessor etlMetricsProcessor = new TestETLMetricsProcessor();

    @BeforeEach
    void setUp() {
        // Reset the test metrics processor
        etlMetricsProcessor.reset();
        lenient().when(exchange.getIn()).thenReturn(message);
    }

    @Test
    void process_WhenOperationProvided_ShouldUpdateMetrics() throws Exception {
        // Arrange
        String operation = "syncCustomers";
        when(message.getHeader("operation", String.class)).thenReturn(operation);

        // Act
        etlMetricsProcessor.process(exchange);

        // Assert
        Map<String, Object> allMetrics = etlMetricsProcessor.getAllMetrics();
        assertTrue(allMetrics.containsKey(operation));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> opMetrics = (Map<String, Object>) allMetrics.get(operation);
        assertEquals(1L, ((Number) opMetrics.get("executionCount")).longValue());
        assertNotNull(opMetrics.get("lastExecutionTime"));
        
        verify(exchange).setProperty(eq("etlMetrics"), any(Map.class));
    }

    @Test
    void process_WhenOperationProvidedMultipleTimes_ShouldIncrementCount() throws Exception {
        // Arrange
        String operation = "syncPayments";
        when(message.getHeader("operation", String.class)).thenReturn(operation);

        // Act
        etlMetricsProcessor.process(exchange);
        etlMetricsProcessor.process(exchange);
        etlMetricsProcessor.process(exchange);

        // Assert
        Map<String, Object> allMetrics = etlMetricsProcessor.getAllMetrics();
        assertTrue(allMetrics.containsKey(operation));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> opMetrics = (Map<String, Object>) allMetrics.get(operation);
        assertEquals(3L, ((Number) opMetrics.get("executionCount")).longValue());
        
        verify(exchange, times(3)).setProperty(eq("etlMetrics"), any(Map.class));
    }

    @Test
    void process_WhenNoOperationProvided_ShouldNotUpdateMetrics() throws Exception {
        // Arrange
        when(message.getHeader("operation", String.class)).thenReturn(null);

        // Act
        etlMetricsProcessor.process(exchange);

        // Assert
        Map<String, Object> metrics = etlMetricsProcessor.getAllMetrics();
        assertTrue(metrics.isEmpty());
        
        verify(exchange, never()).setProperty(eq("etlMetrics"), any(Map.class));
    }



    @Test
    void getAllMetrics_WhenMetricsExist_ShouldReturnAllMetrics() throws Exception {
        // Arrange
        String operation1 = "syncCustomers";
        String operation2 = "syncPayments";
        
        // Add test metrics directly using the test utility method
        etlMetricsProcessor.addTestMetric(operation1, 1, LocalDateTime.now());
        etlMetricsProcessor.addTestMetric(operation2, 2, LocalDateTime.now());

        // Act
        Map<String, Object> metrics = etlMetricsProcessor.getAllMetrics();

        // Assert
        assertNotNull(metrics);
        assertEquals(2, metrics.size());
        assertTrue(metrics.containsKey(operation1));
        assertTrue(metrics.containsKey(operation2));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> op1Metrics = (Map<String, Object>) metrics.get(operation1);
        @SuppressWarnings("unchecked")
        Map<String, Object> op2Metrics = (Map<String, Object>) metrics.get(operation2);
        
        assertEquals(1L, ((Number) op1Metrics.get("executionCount")).longValue());
        assertEquals(2L, ((Number) op2Metrics.get("executionCount")).longValue());
    }
    @Test
    void getAllMetrics_WhenNoMetricsExist_ShouldReturnEmptyMap() {
        // Act
        Map<String, Object> result = etlMetricsProcessor.getAllMetrics();

        // Assert
        assertTrue(result.isEmpty());
    }
}
