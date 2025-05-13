package com.lunar.stripelunar.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApiErrorTest {

    @Test
    void constructor_WithStatusOnly_ShouldSetStatusAndTimestamp() {
        // Act
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, apiError.getStatus());
        assertNotNull(apiError.getTimestamp());
        assertNull(apiError.getMessage());
        assertNull(apiError.getDebugMessage());
        assertNull(apiError.getSubErrors());
    }

    @Test
    void constructor_WithStatusMessageAndException_ShouldSetAllFields() {
        // Arrange
        String message = "Test error message";
        Exception ex = new RuntimeException("Test exception");

        // Act
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, message, ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, apiError.getStatus());
        assertNotNull(apiError.getTimestamp());
        assertEquals(message, apiError.getMessage());
        assertEquals("Test exception", apiError.getDebugMessage());
        assertNull(apiError.getSubErrors());
    }

    @Test
    void builder_ShouldCreateApiErrorWithAllFields() {
        // Arrange
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        LocalDateTime timestamp = LocalDateTime.now();
        String message = "Builder test message";
        String debugMessage = "Builder debug message";
        List<ApiSubError> subErrors = new ArrayList<>();

        // Act
        ApiError apiError = ApiError.builder()
                .status(status)
                .timestamp(timestamp)
                .message(message)
                .debugMessage(debugMessage)
                .subErrors(subErrors)
                .build();

        // Assert
        assertEquals(status, apiError.getStatus());
        assertEquals(timestamp, apiError.getTimestamp());
        assertEquals(message, apiError.getMessage());
        assertEquals(debugMessage, apiError.getDebugMessage());
        assertEquals(subErrors, apiError.getSubErrors());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Arrange
        ApiError apiError = new ApiError();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        LocalDateTime timestamp = LocalDateTime.now();
        String message = "Test message";
        String debugMessage = "Test debug message";
        List<ApiSubError> subErrors = new ArrayList<>();

        // Act
        apiError.setStatus(status);
        apiError.setTimestamp(timestamp);
        apiError.setMessage(message);
        apiError.setDebugMessage(debugMessage);
        apiError.setSubErrors(subErrors);

        // Assert
        assertEquals(status, apiError.getStatus());
        assertEquals(timestamp, apiError.getTimestamp());
        assertEquals(message, apiError.getMessage());
        assertEquals(debugMessage, apiError.getDebugMessage());
        assertEquals(subErrors, apiError.getSubErrors());
    }

    @Test
    void equalsAndHashCode_ShouldWorkCorrectly() {
        // Arrange
        HttpStatus status = HttpStatus.BAD_REQUEST;
        LocalDateTime timestamp = LocalDateTime.now();
        String message = "Test message";
        String debugMessage = "Test debug message";

        ApiError apiError1 = ApiError.builder()
                .status(status)
                .timestamp(timestamp)
                .message(message)
                .debugMessage(debugMessage)
                .build();

        ApiError apiError2 = ApiError.builder()
                .status(status)
                .timestamp(timestamp)
                .message(message)
                .debugMessage(debugMessage)
                .build();

        ApiError apiError3 = ApiError.builder()
                .status(HttpStatus.NOT_FOUND)
                .timestamp(timestamp)
                .message(message)
                .debugMessage(debugMessage)
                .build();

        // Assert
        assertEquals(apiError1, apiError2);
        assertEquals(apiError1.hashCode(), apiError2.hashCode());
        assertNotEquals(apiError1, apiError3);
        assertNotEquals(apiError1.hashCode(), apiError3.hashCode());
    }
}
