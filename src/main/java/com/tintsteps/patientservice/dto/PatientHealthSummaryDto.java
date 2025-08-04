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
public class PatientHealthSummaryDto {
    private PatientDto patient;
    private List<PatientAllergyDto> allergies;
    private List<PatientMedicationDto> currentMedications;
    private List<PatientMedicationDto> allMedications;
    private List<PatientEmergencyContactDto> emergencyContacts;
    private List<PatientInsuranceDto> insurance;
    private List<PatientMedicalHistoryDto> medicalHistory;
    private List<PatientAddressDto> addresses;
    private List<PatientAppointmentDto> appointments;
}
