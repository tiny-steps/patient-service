package com.tintsteps.patientservice.integration.model;

import lombok.Builder;

@Builder
public record UserModel(
        String id,
        String name,
        String email,
        String phone,
        String avatar,
        String status,
        String role,
        String createdAt,
        String updatedAt) {
}
