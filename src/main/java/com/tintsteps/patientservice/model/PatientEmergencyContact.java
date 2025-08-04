package com.tintsteps.patientservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "patient_emergency_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientEmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(nullable = false)
    private String name;

    private String relationship;

    @Column(nullable = false)
    private String phone;
}
