package com.lunar.stripelunar.service;

import com.lunar.stripelunar.model.Customer;
import com.lunar.stripelunar.model.Payment;
import com.lunar.stripelunar.repository.CustomerRepository;
import com.lunar.stripelunar.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private StripeServiceImpl stripeService;

    private Customer testCustomer;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // Setup test data
        testCustomer = new Customer();
        testCustomer.setId("cus_test123");
        testCustomer.setEmail("test@example.com");
        testCustomer.setName("Test Customer");
        testCustomer.setCreatedDate(LocalDateTime.now());
        testCustomer.setUpdatedDate(LocalDateTime.now());

        testPayment = new Payment();
        testPayment.setId("pay_test123");
        testPayment.setCustomerId("cus_test123");
        testPayment.setAmount(new BigDecimal("99.99"));
        testPayment.setCurrency("USD");
        testPayment.setStatus("succeeded");
        testPayment.setCreatedDate(LocalDateTime.now());
        testPayment.setUpdatedDate(LocalDateTime.now());
    }

    @Test
    void getAllCustomers_ShouldReturnAllCustomers() {
        // Arrange
        List<Customer> expectedCustomers = Arrays.asList(testCustomer);
        when(customerRepository.findAll()).thenReturn(expectedCustomers);

        // Act
        List<Customer> actualCustomers = stripeService.getAllCustomers();

        // Assert
        assertEquals(expectedCustomers, actualCustomers);
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void getCustomer_WhenCustomerExists_ShouldReturnCustomer() {
        // Arrange
        String customerId = "cus_test123";
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));

        // Act
        Customer actualCustomer = stripeService.getCustomer(customerId);

        // Assert
        assertEquals(testCustomer, actualCustomer);
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void getCustomer_WhenCustomerDoesNotExist_ShouldThrowException() {
        // Arrange
        String customerId = "cus_nonexistent";
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> stripeService.getCustomer(customerId));
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void getAllPayments_ShouldReturnAllPayments() {
        // Arrange
        List<Payment> expectedPayments = Arrays.asList(testPayment);
        when(paymentRepository.findAll()).thenReturn(expectedPayments);

        // Act
        List<Payment> actualPayments = stripeService.getAllPayments();

        // Assert
        assertEquals(expectedPayments, actualPayments);
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void getPayment_WhenPaymentExists_ShouldReturnPayment() {
        // Arrange
        String paymentId = "pay_test123";
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        // Act
        Payment actualPayment = stripeService.getPayment(paymentId);

        // Assert
        assertEquals(testPayment, actualPayment);
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void getPayment_WhenPaymentDoesNotExist_ShouldThrowException() {
        // Arrange
        String paymentId = "pay_nonexistent";
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> stripeService.getPayment(paymentId));
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void getPaymentsByCustomer_ShouldReturnCustomerPayments() {
        // Arrange
        String customerId = "cus_test123";
        List<Payment> expectedPayments = Arrays.asList(testPayment);
        when(paymentRepository.findByCustomerId(customerId)).thenReturn(expectedPayments);

        // Act
        List<Payment> actualPayments = stripeService.getPaymentsByCustomer(customerId);

        // Assert
        assertEquals(expectedPayments, actualPayments);
        verify(paymentRepository, times(1)).findByCustomerId(customerId);
    }
}
