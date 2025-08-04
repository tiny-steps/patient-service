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
public class PatientCarePlanDto {
    private PatientDto patient;
    private List<PatientMedicationDto> currentMedications;
    private List<String> chronicConditions;
    private List<String> criticalAllergies;
    private List<PatientAppointmentDto> upcomingAppointments;
}
