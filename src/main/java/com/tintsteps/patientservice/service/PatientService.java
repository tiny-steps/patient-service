package com.tintsteps.patientservice.service;

import com.tintsteps.patientservice.dto.PatientDto;
import com.tintsteps.patientservice.model.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface PatientService {

    // CRUD Operations
    PatientDto create(PatientDto patientDto);
    PatientDto findById(UUID id);
    PatientDto findByUserId(UUID userId);
    Page<PatientDto> findAll(Pageable pageable);
    PatientDto update(UUID id, PatientDto patientDto);
    PatientDto partialUpdate(UUID id, PatientDto patientDto);
    void delete(UUID id);

    @Transactional(readOnly = true)
    List<PatientDto> findByGender(Gender gender);

    // Search Operations - Only keep used methods
    Page<PatientDto> findByGender(Gender gender, Pageable pageable);
    Page<PatientDto> findByBloodGroup(String bloodGroup, Pageable pageable);
    Page<PatientDto> findByAgeBetween(Integer minAge, Integer maxAge, Pageable pageable);
    List<PatientDto> findAll();

    // Business Operations
    PatientDto updateMedicalInfo(UUID id, Integer heightCm, BigDecimal weightKg, String bloodGroup);
    PatientDto updatePersonalInfo(UUID id, Date dateOfBirth, Gender gender);
    int calculateAge(UUID id);
    BigDecimal calculateBMI(UUID id);

    // Statistics Operations - Only keep used methods
    long countAll();
    Double getAverageAge();
    Double getAverageHeight();
    BigDecimal getAverageWeight();
    List<Object[]> getGenderStatistics();
    List<Object[]> getBloodGroupStatistics();
    List<String> getDistinctBloodGroups();

    // Bulk Operations
    List<PatientDto> createBatch(List<PatientDto> patientDtos);
    void deleteBatch(List<UUID> ids);

    // Profile Completeness - Only keep used methods
    int calculateProfileCompleteness(UUID id);
    List<String> getMissingProfileFields(UUID id);

    // Additional Operations - Only keep used methods
    boolean hasEmergencyContacts(UUID patientId);

    @Transactional(readOnly = true)
    boolean hasCompleteBasicInfo(UUID id);

    @Transactional(readOnly = true)
    boolean hasMedicalInfo(UUID id);

    Page<PatientDto> findByAgeRange(Integer minAge, Integer maxAge, Pageable pageable);
}
