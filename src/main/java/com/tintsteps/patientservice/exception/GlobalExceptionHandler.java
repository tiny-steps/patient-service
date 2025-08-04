package com.tintsteps.patientservice.exception;

import com.tintsteps.patientservice.model.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ResponseModel<Object>> handlePatientNotFoundException(
            PatientNotFoundException ex, WebRequest request) {
        log.error("Patient not found: {}", ex.getMessage());
        
        ResponseModel<Object> response = ResponseModel.error(
                HttpStatus.NOT_FOUND,
                "Patient not found",
                ex.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PatientServiceException.class)
    public ResponseEntity<ResponseModel<Object>> handlePatientServiceException(
            PatientServiceException ex, WebRequest request) {
        log.error("Patient service error: {}", ex.getMessage(), ex);
        
        ResponseModel<Object> response = ResponseModel.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Patient service error",
                ex.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseModel<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        
        ResponseModel<Object> response = ResponseModel.error(
                HttpStatus.FORBIDDEN,
                "Access denied",
                "You don't have permission to access this resource",
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseModel<Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ResponseModel<Object> response = ResponseModel.error(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors.toString(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseModel<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument: {}", ex.getMessage());
        
        ResponseModel<Object> response = ResponseModel.error(
                HttpStatus.BAD_REQUEST,
                "Invalid argument",
                ex.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseModel<Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ResponseModel<Object> response = ResponseModel.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred",
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
