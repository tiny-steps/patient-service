package com.tinysteps.patientservice.integration.model;

import lombok.Builder;

@SuppressWarnings("unused")
@Builder
public record RegisterRequest (
        String name,
        String email,
        String password,
        String phone,
        String role
){
}
