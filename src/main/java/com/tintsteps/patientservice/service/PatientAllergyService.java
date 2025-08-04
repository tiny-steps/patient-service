package com.tintsteps.patientservice.service;

import com.tintsteps.patientservice.dto.PatientAllergyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface PatientAllergyService {

    // CRUD Operations
    PatientAllergyDto create(PatientAllergyDto patientAllergyDto);
    PatientAllergyDto findById(UUID id);
    Page<PatientAllergyDto> findAll(Pageable pageable);
    PatientAllergyDto update(UUID id, PatientAllergyDto patientAllergyDto);
    PatientAllergyDto partialUpdate(UUID id, PatientAllergyDto patientAllergyDto);
    void delete(UUID id);

    // Patient-specific Operations
    List<PatientAllergyDto> findByPatientId(UUID patientId);
    Page<PatientAllergyDto> findByPatientId(UUID patientId, Pageable pageable);

    // Search Operations
    List<PatientAllergyDto> findByAllergen(String allergen);
    Page<PatientAllergyDto> findByAllergen(String allergen, Pageable pageable);
    List<PatientAllergyDto> findByReaction(String reaction);
    Page<PatientAllergyDto> findByReaction(String reaction, Pageable pageable);
    List<PatientAllergyDto> findByPatientAndAllergen(UUID patientId, String allergen);
    List<PatientAllergyDto> findByDateRange(Timestamp startDate, Timestamp endDate);
    Page<PatientAllergyDto> findByDateRange(Timestamp startDate, Timestamp endDate, Pageable pageable);

    // Advanced Search
    Page<PatientAllergyDto> searchAllergies(UUID patientId, String allergen, String reaction,
                                            Timestamp startDate, Timestamp endDate, Pageable pageable);

    // Business Operations
    PatientAllergyDto addAllergy(UUID patientId, String allergen, String reaction);
    void removeAllergy(UUID patientId, String allergen);
    List<PatientAllergyDto> getAllergiesForPatient(UUID patientId);
    boolean hasAllergy(UUID patientId, String allergen);

    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByPatientId(UUID patientId);
    boolean existsByPatientIdAndAllergen(UUID patientId, String allergen);

    // Statistics Operations
    long countByPatientId(UUID patientId);
    long countByAllergen(String allergen);
    long countAll();
    List<Object[]> getAllergenStatistics();
    List<String> getDistinctAllergens();
    List<String> getDistinctReactions();

    // Bulk Operations
    List<PatientAllergyDto> createBatch(List<PatientAllergyDto> patientAllergyDtos);
    void deleteByPatientId(UUID patientId);
    void deleteBatch(List<UUID> ids);

    // Medical Operations
    List<String> getCriticalAllergies(UUID patientId);
    boolean hasCriticalAllergies(UUID patientId);
    int getAllergyCount(UUID patientId);
}
