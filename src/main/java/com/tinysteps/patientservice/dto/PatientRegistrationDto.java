package com.tinysteps.patientservice.dto;

import com.tinysteps.patientservice.model.Gender;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
public class PatientRegistrationDto {
    private String name;
    private String email;
    private String password;
    private String phone;
    private final String role = "PATIENT";
    private Date dateOfBirth;
    private Gender gender;
    private String bloodGroup;
    private Integer heightCm;
    private BigDecimal weightKg;
    // No id field needed, as PatientDTO will be returned with id after creation
}
