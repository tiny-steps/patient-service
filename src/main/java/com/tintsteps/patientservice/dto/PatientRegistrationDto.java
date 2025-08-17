package com.tintsteps.patientservice.dto;

import com.tintsteps.patientservice.model.Gender;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
public class PatientRegistrationDto {
    private String name;
    private String email;
    private String password;
    private final String role = "PATIENT";
    private Date dateOfBirth;
    private Gender gender;
    private String bloodGroup;
    private Integer heightCm;
    private BigDecimal weightKg;
    // No id field needed, as PatientDTO will be returned with id after creation
}
