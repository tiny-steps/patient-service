package com.tinysteps.patientservice.model;

import com.tinysteps.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(name = "branch_id")
    private UUID branchId;

    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String bloodGroup;

    private Integer heightCm;

    private BigDecimal weightKg;
}
