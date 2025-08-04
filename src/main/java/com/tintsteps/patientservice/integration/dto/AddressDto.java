package com.tintsteps.patientservice.integration.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AddressDto {
    private UUID id;
    private String userId;
    private String type; // HOME, WORK, OTHER
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String landmark;
    private Double latitude;
    private Double longitude;
    private boolean isDefault;
}
