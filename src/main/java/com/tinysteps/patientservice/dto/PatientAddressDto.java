package com.tinysteps.patientservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PatientAddressDto {
    private UUID id;
    private UUID patientId;
    private UUID addressId;
}
