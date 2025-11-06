package com.pillmate.pillmate.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.pillmate.pillmate.DTO.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getReason() != null ? ex.getReason() : ex.getMessage())
                .error(status.name())
                .status(status.value())
                .build();
        
        return ResponseEntity.status(status).body(errorResponse);
    }
}

