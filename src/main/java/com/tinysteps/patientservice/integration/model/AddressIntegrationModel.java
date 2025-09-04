package com.tinysteps.patientservice.integration.model;

import lombok.Builder;

/**
 * Model for Address Service integration
 */
@Builder
public record AddressIntegrationModel(
        String id,
        String userId,
        String type,
        String streetAddress,
        String apartment,
        String city,
        String state,
        String postalCode,
        String country,
        String countryCode,
        double latitude,
        double longitude,
        boolean isDefault,
        String createdAt,
        String updatedAt
) {
}
