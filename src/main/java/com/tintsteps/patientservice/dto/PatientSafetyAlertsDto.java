package com.tintsteps.patientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientSafetyAlertsDto {
    private List<String> criticalAllergies;
    private List<String> activeMedications;
    private List<PatientMedicationDto> expiringMedications;
    private List<String> chronicConditions;
    private boolean missingEmergencyContacts;
    private boolean missingInsurance;
    private boolean hasCriticalAllergies;
    private boolean hasExpiringMedications;
    private boolean hasChronicConditions;
}
