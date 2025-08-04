package com.tintsteps.patientservice.service;

import com.tintsteps.patientservice.dto.PatientMedicalHistoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PatientMedicalHistoryService {

    // CRUD Operations
    PatientMedicalHistoryDto create(PatientMedicalHistoryDto patientMedicalHistoryDto);
    PatientMedicalHistoryDto findById(UUID id);
    List<PatientMedicalHistoryDto> findByPatientId(UUID patientId);
    Page<PatientMedicalHistoryDto> findAll(Pageable pageable);
    PatientMedicalHistoryDto update(UUID id, PatientMedicalHistoryDto patientMedicalHistoryDto);
    PatientMedicalHistoryDto partialUpdate(UUID id, PatientMedicalHistoryDto patientMedicalHistoryDto);
    void delete(UUID id);

    // Search Operations - Only keep used methods
    List<PatientMedicalHistoryDto> findByCondition(String condition);

    Page<PatientMedicalHistoryDto> findByCondition(String condition,Pageable pageable);
    Page<PatientMedicalHistoryDto> searchMedicalHistory(UUID patientId, String condition, String notes,
                                                        Instant startDate, Instant endDate, Pageable pageable);

    // Business Operations - Only keep used methods
    PatientMedicalHistoryDto addMedicalHistory(UUID patientId, String condition, String notes);
    void removeMedicalHistory(UUID patientId, String condition);
    boolean hasMedicalHistory(UUID patientId, String condition);
    PatientMedicalHistoryDto updateMedicalHistory(UUID id, String condition, String notes);

    // Statistics Operations - Only keep used methods
    long countAll();
    List<Object[]> getConditionStatistics();
    List<String> getDistinctConditions();
    List<UUID> findPatientsWithoutMedicalHistory();

    // Bulk Operations - Only keep used methods
    List<PatientMedicalHistoryDto> createBatch(List<PatientMedicalHistoryDto> patientMedicalHistoryDtos);
    void deleteByPatientId(UUID patientId);

    // Medical History Management - Only keep used methods
    List<String> getMedicalConditions(UUID patientId);
    boolean hasMedicalHistory(UUID patientId);
    List<PatientMedicalHistoryDto> getRecentMedicalHistory(UUID patientId, int daysBack);
    List<String> getChronicConditions(UUID patientId);

    int getMedicalHistoryCount(UUID patientId);

    List<UUID> findPatientsWithMultipleHistoryRecords();
}
