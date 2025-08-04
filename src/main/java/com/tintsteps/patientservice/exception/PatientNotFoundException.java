package com.tintsteps.patientservice.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {
    
    public PatientNotFoundException(String message) {
        super(message);
    }
    
    public PatientNotFoundException(UUID id) {
        super("Patient not found with id: " + id);
    }
    
    public PatientNotFoundException(String field, Object value) {
        super("Patient not found with " + field + ": " + value);
    }
    
    public PatientNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
