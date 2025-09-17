package com.tinysteps.patientservice.dto;

import com.tinysteps.common.entity.EntityStatus;
import com.tinysteps.patientservice.model.Gender;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
public class PatientDto {
    private UUID id;
    private UUID userId;
    private UUID branchId;
    private Date dateOfBirth;
    private Gender gender;
    private String bloodGroup;
    private Integer heightCm;
    private BigDecimal weightKg;
    private String name;
    private String email;
    private String phone;
    private EntityStatus status;
}
