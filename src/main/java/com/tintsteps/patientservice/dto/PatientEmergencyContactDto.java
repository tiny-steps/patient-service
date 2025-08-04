package com.tintsteps.patientservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PatientEmergencyContactDto {
    private UUID id;
    private UUID patientId;
    private String name;
    private String relationship;
    private String phone;
}
