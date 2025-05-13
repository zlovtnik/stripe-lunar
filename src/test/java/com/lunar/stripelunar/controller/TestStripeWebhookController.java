package com.lunar.stripelunar.controller;

import com.lunar.stripelunar.service.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test-specific implementation of StripeWebhookController that bypasses Stripe API calls
 * for testing purposes.
 */
public class TestStripeWebhookController extends StripeWebhookController {
    
    private final StripeService stripeService;
    
    public TestStripeWebhookController(StripeService stripeService) {
        super(stripeService);
        this.stripeService = stripeService;
    }
    
    /**
     * Test-specific method to simulate handling a webhook event without using Stripe API
     */
    public ResponseEntity<String> handleTestWebhook(String eventType) {
        try {
            // Process the event based on its type
            switch (eventType) {
                case "customer.created":
                case "customer.updated":
                case "customer.deleted":
                    stripeService.syncCustomers();
                    break;
                case "charge.succeeded":
                case "payment_intent.succeeded":
                    stripeService.syncPayments();
                    break;
                default:
                    // Unhandled event type, just log it
                    break;
            }
            
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }
    
    /**
     * Test-specific method to simulate a deserialization failure
     */
    public ResponseEntity<String> simulateDeserializationFailure() {
        return ResponseEntity.badRequest().body("Failed to deserialize webhook event object");
    }
    
    /**
     * Test-specific method to simulate a signature verification exception
     */
    public ResponseEntity<String> simulateSignatureVerificationException() {
        return ResponseEntity.badRequest().body("Invalid signature");
    }
    
    /**
     * Test-specific method to simulate a generic exception
     */
    public ResponseEntity<String> simulateGenericException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing webhook: Test exception");
    }
}
