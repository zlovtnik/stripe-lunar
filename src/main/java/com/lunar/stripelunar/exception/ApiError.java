package com.lunar.stripelunar.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {
    
    private HttpStatus status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String message;
    private String debugMessage;
    private List<ApiSubError> subErrors;
    
    private ApiError(HttpStatus status) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
    }
    
    public ApiError(HttpStatus status, String message, Throwable ex) {
        this(status);
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
    }
}
