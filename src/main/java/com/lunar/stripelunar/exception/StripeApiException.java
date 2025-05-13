package com.lunar.stripelunar.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StripeApiException extends RuntimeException {
    
    public StripeApiException(String message) {
        super(message);
    }
    
    public StripeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
