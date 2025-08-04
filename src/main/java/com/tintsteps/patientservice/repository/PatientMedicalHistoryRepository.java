package com.tintsteps.patientservice.repository;

import com.tintsteps.patientservice.model.PatientMedicalHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PatientMedicalHistoryRepository extends JpaRepository<PatientMedicalHistory, UUID> {

    // Find by patient ID
    List<PatientMedicalHistory> findByPatientId(UUID patientId);
    Page<PatientMedicalHistory> findByPatientId(UUID patientId, Pageable pageable);

    // Find by condition
    List<PatientMedicalHistory> findByConditionContainingIgnoreCase(String condition);
    Page<PatientMedicalHistory> findByConditionContainingIgnoreCase(String condition, Pageable pageable);

    // Find by notes
    List<PatientMedicalHistory> findByNotesContainingIgnoreCase(String notes);
    Page<PatientMedicalHistory> findByNotesContainingIgnoreCase(String notes, Pageable pageable);

    // Find by patient and condition
    List<PatientMedicalHistory> findByPatientIdAndConditionContainingIgnoreCase(UUID patientId, String condition);

    // Find by recorded date range
    List<PatientMedicalHistory> findByRecordedAtBetween(Instant startDate, Instant endDate);
    Page<PatientMedicalHistory> findByRecordedAtBetween(Instant startDate, Instant endDate, Pageable pageable);

    // Validation methods
    boolean existsByPatientId(UUID patientId);
    boolean existsByPatientIdAndCondition(UUID patientId, String condition);

    // Count methods
    long countByPatientId(UUID patientId);
    long countByConditionContainingIgnoreCase(String condition);

    // Delete methods
    void deleteByPatientId(UUID patientId);

    // Statistics methods
    @Query("SELECT pmh.condition, COUNT(pmh) FROM PatientMedicalHistory pmh WHERE pmh.condition IS NOT NULL GROUP BY pmh.condition ORDER BY COUNT(pmh) DESC")
    List<Object[]> getConditionStatistics();

    @Query("SELECT DISTINCT pmh.condition FROM PatientMedicalHistory pmh WHERE pmh.condition IS NOT NULL ORDER BY pmh.condition")
    List<String> findDistinctConditions();

    // Find patients with multiple medical history records
    @Query("SELECT pmh.patient.id FROM PatientMedicalHistory pmh GROUP BY pmh.patient.id HAVING COUNT(pmh) > 1")
    List<UUID> findPatientsWithMultipleHistoryRecords();

    // Find patients without medical history
    @Query("SELECT p.id FROM Patient p WHERE p.id NOT IN (SELECT DISTINCT pmh.patient.id FROM PatientMedicalHistory pmh)")
    List<UUID> findPatientsWithoutMedicalHistory();

    // Find recent medical history
    @Query("SELECT pmh FROM PatientMedicalHistory pmh WHERE pmh.recordedAt >= :sinceDate ORDER BY pmh.recordedAt DESC")
    List<PatientMedicalHistory> findRecentMedicalHistory(@Param("sinceDate") Instant sinceDate);
}
