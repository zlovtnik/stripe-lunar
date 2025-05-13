package com.lunar.stripelunar.service;

import com.lunar.stripelunar.exception.ResourceNotFoundException;
import com.lunar.stripelunar.exception.StripeApiException;
import com.lunar.stripelunar.model.Customer;
import com.lunar.stripelunar.model.Payment;
import com.lunar.stripelunar.repository.CustomerRepository;
import com.lunar.stripelunar.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;

import com.stripe.param.ChargeListParams;
import com.stripe.param.CustomerListParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// No need for explicit Logger imports with @Slf4j

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeServiceImpl implements StripeService {

    // Logger is already provided by @Slf4j annotation

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public List<Customer> syncCustomers() {
        log.info("Starting customer sync from Stripe");
        List<Customer> syncedCustomers = new ArrayList<>();
        
        try {
            CustomerListParams params = CustomerListParams.builder()
                    .setLimit(100L)
                    .build();
            
            com.stripe.model.CustomerCollection customerCollection = com.stripe.model.Customer.list(params);
            
            for (com.stripe.model.Customer stripeCustomer : customerCollection.getData()) {
                Customer customer = mapStripeCustomerToEntity(stripeCustomer);
                customerRepository.save(customer);
                syncedCustomers.add(customer);
            }
            
            log.info("Successfully synced {} customers from Stripe", syncedCustomers.size());
        } catch (StripeException e) {
            log.error("Error syncing customers from Stripe: {}", e.getMessage(), e);
            throw new StripeApiException("Failed to sync customers from Stripe", e);
        }
        
        return syncedCustomers;
    }

    @Override
    public Customer getCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
    }

    @Override
    @Transactional
    public List<Payment> syncPayments() {
        log.info("Starting payment sync from Stripe");
        List<Payment> syncedPayments = new ArrayList<>();
        
        try {
            ChargeListParams params = ChargeListParams.builder()
                    .setLimit(100L)
                    .build();
            
            com.stripe.model.ChargeCollection chargeCollection = Charge.list(params);
            
            for (Charge stripeCharge : chargeCollection.getData()) {
                Payment payment = mapStripeChargeToEntity(stripeCharge);
                paymentRepository.save(payment);
                syncedPayments.add(payment);
            }
            
            log.info("Successfully synced {} payments from Stripe", syncedPayments.size());
        } catch (StripeException e) {
            log.error("Error syncing payments from Stripe: {}", e.getMessage(), e);
            throw new StripeApiException("Failed to sync payments from Stripe", e);
        }
        
        return syncedPayments;
    }

    @Override
    public List<Payment> getPaymentsByCustomer(String customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }

    @Override
    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
    }
    
    @Override
    public List<Customer> getAllCustomers() {
        log.info("Retrieving all customers from database");
        return customerRepository.findAll();
    }
    
    @Override
    public List<Payment> getAllPayments() {
        log.info("Retrieving all payments from database");
        return paymentRepository.findAll();
    }
    
    // Helper methods to map Stripe objects to our entities
    private Customer mapStripeCustomerToEntity(com.stripe.model.Customer stripeCustomer) {
        Customer customer = new Customer();
        customer.setId(stripeCustomer.getId());
        customer.setEmail(stripeCustomer.getEmail());
        customer.setName(stripeCustomer.getName());
        customer.setDescription(stripeCustomer.getDescription());
        customer.setCreatedDate(convertTimestampToLocalDateTime(stripeCustomer.getCreated()));
        customer.setUpdatedDate(LocalDateTime.now());
        customer.setMetadata(stripeCustomer.getMetadata() != null ? stripeCustomer.getMetadata().toString() : null);
        customer.setDeleted(stripeCustomer.getDeleted() != null ? stripeCustomer.getDeleted() : false);
        
        return customer;
    }
    
    private Payment mapStripeChargeToEntity(Charge stripeCharge) {
        Payment payment = new Payment();
        payment.setId(stripeCharge.getId());
        payment.setCustomerId(stripeCharge.getCustomer());
        payment.setAmount(BigDecimal.valueOf(stripeCharge.getAmount()).divide(BigDecimal.valueOf(100))); // Convert from cents
        payment.setCurrency(stripeCharge.getCurrency());
        payment.setStatus(stripeCharge.getStatus());
        payment.setDescription(stripeCharge.getDescription());
        payment.setCreatedDate(convertTimestampToLocalDateTime(stripeCharge.getCreated()));
        payment.setUpdatedDate(LocalDateTime.now());
        payment.setMetadata(stripeCharge.getMetadata() != null ? stripeCharge.getMetadata().toString() : null);
        
        return payment;
    }
    
    private LocalDateTime convertTimestampToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }
}

