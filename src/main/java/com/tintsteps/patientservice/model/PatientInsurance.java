package com.tintsteps.patientservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "patient_insurance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private String provider;

    private String policyNumber;

    private String coverageDetails;
}
