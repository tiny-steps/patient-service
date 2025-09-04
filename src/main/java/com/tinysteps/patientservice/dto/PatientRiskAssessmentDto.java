package com.tinysteps.patientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRiskAssessmentDto {
    private UUID patientId;
    private int riskScore;
    private String riskLevel;
    private boolean hasCriticalAllergies;
    private boolean hasChronicConditions;
    private boolean hasMultipleMedications;
    private boolean missingEmergencyContacts;
    private boolean missingInsurance;
}
