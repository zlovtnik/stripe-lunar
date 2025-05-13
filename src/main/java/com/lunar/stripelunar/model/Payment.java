package com.lunar.stripelunar.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "STRIPE_PAYMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @Column(name = "PAYMENT_ID")
    private String id;
    
    @Column(name = "CUSTOMER_ID")
    private String customerId;
    
    @Column(name = "AMOUNT")
    private BigDecimal amount;
    
    @Column(name = "CURRENCY")
    private String currency;
    
    @Column(name = "STATUS")
    private String status;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
    
    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
    
    @Column(name = "METADATA", length = 4000)
    private String metadata;
}
