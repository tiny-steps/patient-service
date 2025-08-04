package com.tintsteps.patientservice.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PatientMedicalHistoryDto {
    private UUID id;
    private UUID patientId;
    private String condition;
    private String notes;
    private Instant recordedAt;
}
