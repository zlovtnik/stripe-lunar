package com.lunar.stripelunar.service;

import com.lunar.stripelunar.model.Customer;
import com.lunar.stripelunar.model.Payment;

import java.util.List;

public interface StripeService {
    
    // Customer operations
    List<Customer> syncCustomers();
    Customer getCustomer(String customerId);
    List<Customer> getAllCustomers();
    
    // Payment operations
    List<Payment> syncPayments();
    List<Payment> getPaymentsByCustomer(String customerId);
    Payment getPayment(String paymentId);
    List<Payment> getAllPayments();
}
