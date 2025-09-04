package com.tinysteps.patientservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PatientAllergyDto {
    private UUID id;
    private UUID patientId;
    private String allergen;
    private String reaction;
}
