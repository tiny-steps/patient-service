package com.tintsteps.patientservice.integration;

import lombok.Builder;

@Builder
public record AuthServiceErrorModel(
        String field,
        String message) {
}
