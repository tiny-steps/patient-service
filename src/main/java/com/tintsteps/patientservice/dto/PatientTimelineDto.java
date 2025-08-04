package com.tintsteps.patientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientTimelineDto {
    private UUID patientId;
    private int daysBack;
    private List<PatientMedicalHistoryDto> medicalHistory;
    private List<PatientMedicationDto> medicationHistory;
    private List<PatientAppointmentDto> appointments;
}
