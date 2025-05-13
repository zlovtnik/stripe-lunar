package com.lunar.stripelunar.controller;

import com.lunar.stripelunar.component.TestETLMetricsProcessor;
import com.lunar.stripelunar.model.Customer;
import com.lunar.stripelunar.model.Payment;
import com.lunar.stripelunar.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ETLControllerTest {

    @Mock
    private StripeService stripeService;

    private TestETLMetricsProcessor etlMetricsProcessor = new TestETLMetricsProcessor();

    @InjectMocks
    private ETLController etlController;

    private List<Customer> mockCustomers;
    private List<Payment> mockPayments;

    @BeforeEach
    void setUp() {
        // Setup mock data
        mockCustomers = new ArrayList<>();
        Customer customer1 = new Customer();
        customer1.setId("cus_123");
        customer1.setName("Test Customer 1");
        customer1.setEmail("test1@example.com");
        customer1.setCreatedDate(LocalDateTime.now().minusDays(10));
        mockCustomers.add(customer1);
        
        Customer customer2 = new Customer();
        customer2.setId("cus_456");
        customer2.setName("Test Customer 2");
        customer2.setEmail("test2@example.com");
        customer2.setCreatedDate(LocalDateTime.now().minusDays(5));
        mockCustomers.add(customer2);

        mockPayments = new ArrayList<>();
        Payment payment1 = new Payment();
        payment1.setId("py_123");
        payment1.setCustomerId("cus_123");
        payment1.setAmount(new java.math.BigDecimal("100.00"));
        payment1.setCurrency("usd");
        payment1.setStatus("succeeded");
        payment1.setCreatedDate(LocalDateTime.now().minusDays(3));
        mockPayments.add(payment1);
        
        Payment payment2 = new Payment();
        payment2.setId("py_456");
        payment2.setCustomerId("cus_456");
        payment2.setAmount(new java.math.BigDecimal("200.00"));
        payment2.setCurrency("usd");
        payment2.setStatus("succeeded");
        payment2.setCreatedDate(LocalDateTime.now().minusDays(1));
        mockPayments.add(payment2);

        // Manually set the ETLMetricsProcessor
        ReflectionTestUtils.setField(etlController, "etlMetricsProcessor", etlMetricsProcessor);
    }

    @Test
    void syncAll_ShouldSyncCustomersAndPayments() {
        // Arrange
        when(stripeService.syncCustomers()).thenReturn(mockCustomers);
        when(stripeService.syncPayments()).thenReturn(mockPayments);

        // Act
        ResponseEntity<Map<String, Object>> response = etlController.syncAll();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Fix: Use Integer instead of Long for the count assertions
        assertEquals(2, response.getBody().get("customersCount"));
        assertEquals(2, response.getBody().get("paymentsCount"));
        assertEquals("completed", response.getBody().get("status"));
        
        verify(stripeService, times(1)).syncCustomers();
        verify(stripeService, times(1)).syncPayments();
    }

    /* Test removed - no syncCustomers endpoint in ETLController */

    /* Test removed - no syncPayments endpoint in ETLController */

    @Test
    void getStatus_ShouldReturnETLStatus() {
        // Arrange
        when(stripeService.getAllCustomers()).thenReturn(mockCustomers);
        when(stripeService.getAllPayments()).thenReturn(mockPayments);
        
        // Add test metrics directly to the TestETLMetricsProcessor
        etlMetricsProcessor.reset();
        etlMetricsProcessor.addTestMetric("syncCustomers", 100, LocalDateTime.now().minusDays(1));
        etlMetricsProcessor.addTestMetric("syncPayments", 250, LocalDateTime.now().minusDays(1));
        etlMetricsProcessor.addTestMetric("syncAll", 350, LocalDateTime.now().minusDays(7));

        // Act
        ResponseEntity<Map<String, Object>> response = etlController.getStatus();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().get("customersCount"));
        assertEquals(2L, response.getBody().get("paymentsCount"));
        assertNotNull(response.getBody().get("metrics"));
        assertNotNull(response.getBody().get("lastSyncTimes"));
        assertEquals("healthy", response.getBody().get("applicationStatus"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> lastSyncTimes = (Map<String, Object>) response.getBody().get("lastSyncTimes");
        assertNotNull(lastSyncTimes.get("customers"));
        assertNotNull(lastSyncTimes.get("payments"));
        assertNotNull(lastSyncTimes.get("fullSync"));
        
        verify(stripeService, times(1)).getAllCustomers();
        verify(stripeService, times(1)).getAllPayments();
    }
}
