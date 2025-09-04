package com.tinysteps.patientservice.integration.model;

import lombok.Builder;

import java.util.List;

/**
 * Generic response model for external service integrations
 */
@Builder
public record IntegrationResponseModel<T>(
        String status,
        int code,
        String message,
        T data,
        List<IntegrationErrorModel> errors
) {
}
