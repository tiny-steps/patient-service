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
public class MedicationSafetyDto {
    private List<String> currentMedications;
    private List<String> allergens;
    private String newMedication;
    private boolean hasConflicts;
    private boolean hasAllergyConflict;
    private List<String> potentialInteractions;
    private boolean isSafeToAdd;
}
