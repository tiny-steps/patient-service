package com.tinysteps.patientservice.exception;

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

    public PatientNotFoundException(String patientAllergy, String id, UUID id1) {
        super("Patient allergy not found with patient id: " + id1 + " and allergy id: " + id);
    }
}
