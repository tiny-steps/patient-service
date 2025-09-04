package com.tinysteps.patientservice.service;

import com.tinysteps.patientservice.dto.PatientMedicationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface PatientMedicationService {

    // CRUD Operations
    PatientMedicationDto create(PatientMedicationDto patientMedicationDto);
    PatientMedicationDto findById(UUID id);
    Page<PatientMedicationDto> findAll(Pageable pageable);
    PatientMedicationDto update(UUID id, PatientMedicationDto patientMedicationDto);
    PatientMedicationDto partialUpdate(UUID id, PatientMedicationDto patientMedicationDto);
    void delete(UUID id);

    // Patient-specific Operations
    List<PatientMedicationDto> findByPatientId(UUID patientId);
    Page<PatientMedicationDto> findByPatientId(UUID patientId, Pageable pageable);

    // Search Operations - Only keep used methods
    List<PatientMedicationDto> findByMedicationName(String medicationName);
    Page<PatientMedicationDto> findByMedicationName(String medicationName, Pageable pageable);
    Page<PatientMedicationDto> searchMedications(UUID patientId, String medicationName, String dosage,
                                                Date startDate, Date endDate, Pageable pageable);

    // Business Operations - Only keep used methods
    PatientMedicationDto addMedication(UUID patientId, String medicationName, String dosage, Date startDate, Date endDate);
    PatientMedicationDto startMedication(UUID patientId, String medicationName, String dosage);
    PatientMedicationDto stopMedication(UUID patientId, String medicationName, Date endDate);
    void discontinueMedication(UUID patientId, String medicationName);
    List<PatientMedicationDto> getCurrentMedicationsForPatient(UUID patientId);
    boolean isOnMedication(UUID patientId, String medicationName);

    // Statistics Operations - Only keep used methods
    long countAll();
    List<Object[]> getMedicationStatistics();
    List<String> getDistinctMedicationNames();
    List<String> getDistinctDosages();

    // Bulk Operations - Only keep used methods
    List<PatientMedicationDto> createBatch(List<PatientMedicationDto> patientMedicationDtos);
    void deleteByPatientId(UUID patientId);

    // Medical Operations - Only keep used methods
    List<String> getActiveMedicationNames(UUID patientId);
    int getActiveMedicationCount(UUID patientId);
    List<PatientMedicationDto> getMedicationHistory(UUID patientId);
    boolean hasMedicationConflicts(UUID patientId, String newMedication);
    List<String> getPotentialInteractions(UUID patientId, String medicationName);
    List<PatientMedicationDto> getExpiringMedications(UUID patientId, int daysAhead);

    Page<PatientMedicationDto> findByDosage(String dosage, Pageable pageable);

    PatientMedicationDto renewMedication(UUID medicationId, Date newEndDate);

    PatientMedicationDto changeDosage(UUID medicationId, String newDosage);
}
