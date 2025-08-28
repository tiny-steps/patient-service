package com.tintsteps.patientservice.integration;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.util.List;

@Builder
public record AuthServiceResponseModel<T>(
        HttpStatus status,
        int code,
        String message,
        T data,
        List<AuthServiceErrorModel> errors) {
}
