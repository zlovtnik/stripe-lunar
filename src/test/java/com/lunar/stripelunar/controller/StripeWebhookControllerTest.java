package com.lunar.stripelunar.controller;

import com.lunar.stripelunar.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeWebhookControllerTest {

    @Mock
    private StripeService stripeService;

    private TestStripeWebhookController testWebhookController;

    @BeforeEach
    void setUp() {
        testWebhookController = new TestStripeWebhookController(stripeService);
    }

    @Test
    void handleStripeWebhook_WithCustomerEvent_ShouldSyncCustomers() {
        // Act
        ResponseEntity<String> response = testWebhookController.handleTestWebhook("customer.created");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Webhook processed successfully", response.getBody());
        verify(stripeService, times(1)).syncCustomers();
        verify(stripeService, never()).syncPayments();
    }

    @Test
    void handleStripeWebhook_WithPaymentEvent_ShouldSyncPayments() {
        // Act
        ResponseEntity<String> response = testWebhookController.handleTestWebhook("charge.succeeded");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Webhook processed successfully", response.getBody());
        verify(stripeService, never()).syncCustomers();
        verify(stripeService, times(1)).syncPayments();
    }

    @Test
    void handleStripeWebhook_WithUnhandledEvent_ShouldNotSync() {
        // Act
        ResponseEntity<String> response = testWebhookController.handleTestWebhook("unhandled.event");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Webhook processed successfully", response.getBody());
        verify(stripeService, never()).syncCustomers();
        verify(stripeService, never()).syncPayments();
    }

    @Test
    void handleStripeWebhook_WithDeserializationFailure_ShouldReturnBadRequest() {
        // Act
        ResponseEntity<String> response = testWebhookController.simulateDeserializationFailure();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Failed to deserialize webhook event object", response.getBody());
        verify(stripeService, never()).syncCustomers();
        verify(stripeService, never()).syncPayments();
    }

    @Test
    void handleStripeWebhook_WithInvalidSignature_ShouldReturnBadRequest() {
        // Act
        ResponseEntity<String> response = testWebhookController.simulateSignatureVerificationException();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid signature", response.getBody());
        verify(stripeService, never()).syncCustomers();
        verify(stripeService, never()).syncPayments();
    }

    @Test
    void handleStripeWebhook_WithGenericException_ShouldReturnInternalServerError() {
        // Act
        ResponseEntity<String> response = testWebhookController.simulateGenericException();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error processing webhook: Test exception", response.getBody());
        verify(stripeService, never()).syncCustomers();
        verify(stripeService, never()).syncPayments();
    }
}
