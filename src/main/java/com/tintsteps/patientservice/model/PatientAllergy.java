package com.tintsteps.patientservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient_allergies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientAllergy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(nullable = false)
    private String allergen;

    private String reaction;

    @CreationTimestamp
    private Instant recordedAt;
}
