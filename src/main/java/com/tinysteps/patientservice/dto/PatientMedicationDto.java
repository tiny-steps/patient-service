package com.tinysteps.patientservice.dto;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class PatientMedicationDto {
    private UUID id;
    private UUID patientId;
    private String medicationName;
    private String dosage;
    private Date startDate;
    private Date endDate;
}
