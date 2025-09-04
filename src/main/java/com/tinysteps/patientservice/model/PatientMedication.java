package com.tinysteps.patientservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "patient_medications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(nullable = false)
    private String medicationName;

    private String dosage;

    private Date startDate;

    private Date endDate;
}
