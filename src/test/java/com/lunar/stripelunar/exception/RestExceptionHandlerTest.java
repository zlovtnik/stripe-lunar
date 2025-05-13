package com.lunar.stripelunar.exception;

import com.stripe.exception.StripeException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RestExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private RestExceptionHandler restExceptionHandler;

    @Test
    void handleHttpMessageNotReadable_ShouldReturnBadRequest() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        HttpStatusCode status = HttpStatus.BAD_REQUEST;
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
            "Test message not readable", new IOException("Test IO exception"), null);

        // Act
        ResponseEntity<Object> response = restExceptionHandler.handleHttpMessageNotReadable(
                ex, headers, status, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiError);
        
        ApiError error = (ApiError) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals("Malformed JSON request", error.getMessage());
        assertNotNull(error.getDebugMessage());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void handleEntityNotFound_ShouldReturnNotFound() {
        // Arrange
        EntityNotFoundException ex = new EntityNotFoundException("Entity not found");

        // Act
        ResponseEntity<Object> response = restExceptionHandler.handleEntityNotFound(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiError);
        
        ApiError error = (ApiError) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertEquals("Entity not found", error.getMessage());
        assertEquals("Entity not found", error.getDebugMessage());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void handleResourceNotFound_ShouldReturnNotFound() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Customer", "id", 123L);

        // Act
        ResponseEntity<Object> response = restExceptionHandler.handleResourceNotFound(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiError);
        
        ApiError error = (ApiError) response.getBody();
        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        assertTrue(error.getMessage().contains("Customer"));
        assertTrue(error.getMessage().contains("id"));
        assertTrue(error.getMessage().contains("123"));
        assertNotNull(error.getTimestamp());
    }

    @Test
    void handleStripeApiException_ShouldReturnBadRequest() {
        // Arrange
        StripeApiException ex = new StripeApiException("Stripe API error");

        // Act
        ResponseEntity<Object> response = restExceptionHandler.handleStripeApiException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiError);
        
        ApiError error = (ApiError) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals("Stripe API error", error.getMessage());
        assertEquals("Stripe API error", error.getDebugMessage());
        assertNotNull(error.getTimestamp());
    }

    // Test implementation of StripeException for testing purposes
    private static class TestStripeException extends StripeException {
        public TestStripeException(String message) {
            super(message, null, null, 0);
        }
    }
    
    @Test
    void handleStripeException_ShouldReturnBadRequest() {
        // Arrange
        StripeException ex = new TestStripeException("Stripe exception message");

        // Act
        ResponseEntity<Object> response = restExceptionHandler.handleStripeException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiError);
        
        ApiError error = (ApiError) response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertTrue(error.getMessage().contains("Error communicating with Stripe API"));
        assertTrue(error.getMessage().contains("Stripe exception message"));
        assertNotNull(error.getDebugMessage());
        assertNotNull(error.getTimestamp());
    }

    @Test
    void handleAllExceptions_ShouldReturnInternalServerError() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<Object> response = restExceptionHandler.handleAllExceptions(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ApiError);
        
        ApiError error = (ApiError) response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatus());
        assertEquals("An unexpected error occurred", error.getMessage());
        assertEquals("Unexpected error", error.getDebugMessage());
        assertNotNull(error.getTimestamp());
    }
}
