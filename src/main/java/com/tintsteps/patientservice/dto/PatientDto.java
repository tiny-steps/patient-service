package com.tintsteps.patientservice.dto;

import com.tintsteps.patientservice.model.Gender;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
public class PatientDto {
    private UUID id;
    private Date dateOfBirth;
    private Gender gender;
    private String bloodGroup;
    private Integer heightCm;
    private BigDecimal weightKg;
}
