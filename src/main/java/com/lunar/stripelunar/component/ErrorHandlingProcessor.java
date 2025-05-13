package com.lunar.stripelunar.component;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ErrorHandlingProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        
        if (exception != null) {
            log.error("Error during ETL process: {}", exception.getMessage(), exception);
            
            // Create error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", exception.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            errorResponse.put("errorType", exception.getClass().getSimpleName());
            
            // Set error response as the body
            exchange.getMessage().setBody(errorResponse);
            
            // Set HTTP status code to 500 for internal server errors
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
        }
    }
}
