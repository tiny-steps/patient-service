package com.tinysteps.patientservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PatientInsuranceDto {
    private UUID id;
    private UUID patientId;
    private String provider;
    private String policyNumber;
    private String coverageDetails;
}
