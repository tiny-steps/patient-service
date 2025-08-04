package com.tintsteps.patientservice.repository;

import com.tintsteps.patientservice.model.PatientMedication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface PatientMedicationRepository extends JpaRepository<PatientMedication, UUID> {

    // Find by patient ID
    List<PatientMedication> findByPatientId(UUID patientId);
    Page<PatientMedication> findByPatientId(UUID patientId, Pageable pageable);

    // Find by medication name
    List<PatientMedication> findByMedicationNameContainingIgnoreCase(String medicationName);
    Page<PatientMedication> findByMedicationNameContainingIgnoreCase(String medicationName, Pageable pageable);

    // Find by dosage
    List<PatientMedication> findByDosageContainingIgnoreCase(String dosage);
    Page<PatientMedication> findByDosageContainingIgnoreCase(String dosage, Pageable pageable);

    // Find by patient and medication
    List<PatientMedication> findByPatientIdAndMedicationNameContainingIgnoreCase(UUID patientId, String medicationName);

    // Find by date ranges
    List<PatientMedication> findByStartDateBetween(Date startDate, Date endDate);
    List<PatientMedication> findByEndDateBetween(Date startDate, Date endDate);
    Page<PatientMedication> findByStartDateBetween(Date startDate, Date endDate, Pageable pageable);

    // Find current medications (no end date or end date in future)
    @Query("SELECT pm FROM PatientMedication pm WHERE pm.patient.id = :patientId AND (pm.endDate IS NULL OR pm.endDate > CURRENT_DATE)")
    List<PatientMedication> findCurrentMedicationsByPatientId(UUID patientId);

    @Query("SELECT pm FROM PatientMedication pm WHERE pm.patient.id = :patientId AND (pm.endDate IS NULL OR pm.endDate > CURRENT_DATE)")
    Page<PatientMedication> findCurrentMedicationsByPatientId(UUID patientId, Pageable pageable);

    // Find past medications
    @Query("SELECT pm FROM PatientMedication pm WHERE pm.patient.id = :patientId AND pm.endDate IS NOT NULL AND pm.endDate <= CURRENT_DATE")
    List<PatientMedication> findPastMedicationsByPatientId(UUID patientId);

    // Validation methods
    boolean existsByPatientId(UUID patientId);
    boolean existsByPatientIdAndMedicationName(UUID patientId, String medicationName);

    // Count methods
    long countByPatientId(UUID patientId);
    long countByMedicationNameContainingIgnoreCase(String medicationName);

    @Query("SELECT COUNT(pm) FROM PatientMedication pm WHERE pm.patient.id = :patientId AND (pm.endDate IS NULL OR pm.endDate > CURRENT_DATE)")
    long countCurrentMedicationsByPatientId(UUID patientId);

    // Delete methods
    void deleteByPatientId(UUID patientId);

    // Statistics methods
    @Query("SELECT pm.medicationName, COUNT(pm) FROM PatientMedication pm GROUP BY pm.medicationName ORDER BY COUNT(pm) DESC")
    List<Object[]> getMedicationStatistics();

    @Query("SELECT DISTINCT pm.medicationName FROM PatientMedication pm WHERE pm.medicationName IS NOT NULL ORDER BY pm.medicationName")
    List<String> findDistinctMedicationNames();

    @Query("SELECT DISTINCT pm.dosage FROM PatientMedication pm WHERE pm.dosage IS NOT NULL ORDER BY pm.dosage")
    List<String> findDistinctDosages();
}
