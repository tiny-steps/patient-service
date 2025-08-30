package com.tintsteps.patientservice.exception;

/**
 * Exception thrown when external service integration fails
 */
public class IntegrationException extends BaseException {

    public IntegrationException(String serviceName, String message) {
        super(
            String.format("Integration with %s failed: %s", serviceName, message),
            "INTEGRATION_FAILURE",
            String.format("Service: %s, Error: %s", serviceName, message)
        );
    }

    public IntegrationException(String serviceName, String message, Throwable cause) {
        super(
            String.format("Integration with %s failed: %s", serviceName, message),
            "INTEGRATION_FAILURE",
            String.format("Service: %s, Error: %s", serviceName, message),
            cause
        );
    }
}
