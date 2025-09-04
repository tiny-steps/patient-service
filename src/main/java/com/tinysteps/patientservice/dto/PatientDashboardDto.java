package com.tinysteps.patientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDashboardDto {
    private PatientDto patient;
    private int profileCompleteness;
    private List<String> criticalAllergies;
    private List<String> activeMedications;
    private List<PatientMedicationDto> expiringMedications;
    private List<PatientEmergencyContactDto> emergencyContacts;
    private List<PatientMedicalHistoryDto> recentMedicalHistory;
    private boolean hasInsurance;
    private boolean hasEmergencyContacts;
    private boolean hasCriticalAllergies;
}
