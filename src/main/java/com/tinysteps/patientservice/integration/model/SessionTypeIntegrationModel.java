package com.tinysteps.patientservice.integration.model;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Model for Session Service integration
 */
@Builder
public record SessionTypeIntegrationModel(
        String id,
        String name,
        String description,
        Integer durationMinutes,
        BigDecimal defaultPrice,
        String category,
        boolean isActive,
        String createdAt,
        String updatedAt
) {
}
