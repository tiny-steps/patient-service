package com.tintsteps.patientservice.integration.model;

import lombok.Builder;

/**
 * Model for User Service integration
 */
@Builder
public record UserIntegrationModel(
        String id,
        String name,
        String email,
        String phone,
        String avatar,
        String status,
        String role,
        String createdAt,
        String updatedAt
) {
}
