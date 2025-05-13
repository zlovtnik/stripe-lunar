package com.lunar.stripelunar.controller;

import com.lunar.stripelunar.component.TestETLMetricsProcessor;
import com.lunar.stripelunar.repository.CustomerRepository;
import com.lunar.stripelunar.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HealthControllerTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private TestETLMetricsProcessor etlMetricsProcessor;

    @InjectMocks
    private HealthController healthController;

    @BeforeEach
    void setUp() {
        // Create a fresh processor for each test
        etlMetricsProcessor = new TestETLMetricsProcessor();
        // Manually set the etlMetricsProcessor in the controller
        ReflectionTestUtils.setField(healthController, "etlMetricsProcessor", etlMetricsProcessor);
    }

    @Test
    void healthCheck_WhenAllSystemsUp_ShouldReturnOkStatus() {
        // Arrange
        when(customerRepository.count()).thenReturn(100L);
        when(paymentRepository.count()).thenReturn(250L);
        etlMetricsProcessor.addTestMetric("syncCustomers", 100, LocalDateTime.now().minusDays(1));
        etlMetricsProcessor.addTestMetric("syncPayments", 250, LocalDateTime.now().minusDays(1));

        // Act
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> database = (Map<String, Object>) response.getBody().get("database");
        assertNotNull(database);
        assertEquals("UP", database.get("status"));
        assertEquals(100L, database.get("customerCount"));
        assertEquals(250L, database.get("paymentCount"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> etlMetrics = (Map<String, Object>) response.getBody().get("etl");
        assertNotNull(etlMetrics);
        assertEquals("UP", etlMetrics.get("status"));
        assertEquals(2, etlMetrics.get("operationsCount"));
        
        verify(customerRepository, times(1)).count();
        verify(paymentRepository, times(1)).count();
    }

    @Test
    void healthCheck_WhenDatabaseDown_ShouldReportDatabaseDown() {
        // Arrange
        when(customerRepository.count()).thenThrow(new RuntimeException("Database connection error"));
        etlMetricsProcessor.addTestMetric("syncCustomers", 100, LocalDateTime.now().minusDays(1));
        etlMetricsProcessor.addTestMetric("syncPayments", 250, LocalDateTime.now().minusDays(1));

        // Act
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> database = (Map<String, Object>) response.getBody().get("database");
        assertNotNull(database);
        assertEquals("DOWN", database.get("status"));
        assertNotNull(database.get("error"));
        
        verify(customerRepository, times(1)).count();
        verify(paymentRepository, never()).count();
    }

    @Test
    void healthCheck_WhenETLMetricsDown_ShouldReportETLDown() {
        // Arrange
        when(customerRepository.count()).thenReturn(100L);
        when(paymentRepository.count()).thenReturn(250L);
        
        // Create a failing processor that throws an exception
        TestETLMetricsProcessor failingProcessor = new TestETLMetricsProcessor() {
            @Override
            public Map<String, Object> getAllMetrics() {
                throw new RuntimeException("ETL metrics unavailable");
            }
        };
        
        // Replace the processor in the controller with the failing one
        ReflectionTestUtils.setField(healthController, "etlMetricsProcessor", failingProcessor);

        // Act
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> database = (Map<String, Object>) response.getBody().get("database");
        assertNotNull(database);
        assertEquals("UP", database.get("status"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> etlMetrics = (Map<String, Object>) response.getBody().get("etl");
        assertNotNull(etlMetrics);
        assertEquals("DOWN", etlMetrics.get("status"));
        assertNotNull(etlMetrics.get("error"));
        
        verify(customerRepository, times(1)).count();
        verify(paymentRepository, times(1)).count();
    }
}
