package com.tinysteps.patientservice.exception;

import lombok.Getter;

/**
 * Base exception class for all custom exceptions in the doctor service
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final String errorCode;
    private final String details;

    protected BaseException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    protected BaseException(String message, String errorCode, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
}
