package com.lunar.stripelunar.controller;

import com.lunar.stripelunar.service.StripeService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final StripeService stripeService;
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/webhook/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        log.info("Received Stripe webhook event");
        
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            
            // Deserialize the event data
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            
            if (!dataObjectDeserializer.getObject().isPresent()) {
                log.warn("Failed to deserialize webhook event object");
                return ResponseEntity.badRequest().body("Failed to deserialize webhook event object");
            }
            
            // Successfully deserialized the object
            StripeObject stripeObject = dataObjectDeserializer.getObject().get();
            log.debug("Successfully deserialized webhook event object: {}", stripeObject.getClass().getSimpleName());
            
            // Handle the event type
            switch (event.getType()) {
                case "customer.created":
                case "customer.updated":
                case "customer.deleted":
                    log.info("Customer event received: {}", event.getType());
                    // Trigger customer sync
                    stripeService.syncCustomers();
                    break;
                    
                case "charge.succeeded":
                case "charge.failed":
                case "charge.refunded":
                    log.info("Payment event received: {}", event.getType());
                    // Trigger payment sync
                    stripeService.syncPayments();
                    break;
                    
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }
            
            return ResponseEntity.ok().body("Webhook processed successfully");
            
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature on Stripe webhook", e);
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            log.error("Error processing Stripe webhook", e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
}
