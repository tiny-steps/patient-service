package com.tinysteps.patientservice.service;

import com.tinysteps.patientservice.dto.PatientEmergencyContactDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PatientEmergencyContactService {

    // CRUD Operations
    PatientEmergencyContactDto create(PatientEmergencyContactDto patientEmergencyContactDto);
    PatientEmergencyContactDto findById(UUID id);
    List<PatientEmergencyContactDto> findByPatientId(UUID patientId);
    Page<PatientEmergencyContactDto> findAll(Pageable pageable);
    PatientEmergencyContactDto update(UUID id, PatientEmergencyContactDto patientEmergencyContactDto);
    PatientEmergencyContactDto partialUpdate(UUID id, PatientEmergencyContactDto patientEmergencyContactDto);
    void delete(UUID id);

    // Search Operations
    Page<PatientEmergencyContactDto> findByRelationship(String relationship, Pageable pageable);
    Page<PatientEmergencyContactDto> findByPatientId(UUID patientId, Pageable pageable);

    // Advanced Search
    Page<PatientEmergencyContactDto> searchContacts(UUID patientId, String name, String relationship,
                                                   String phone, Pageable pageable);

    // Business Operations
    PatientEmergencyContactDto addEmergencyContact(UUID patientId, String name, String relationship, String phone);
    void removeEmergencyContact(UUID patientId, String phone);
    boolean hasEmergencyContact(UUID patientId, String phone);
    PatientEmergencyContactDto updateContactInfo(UUID id, String name, String relationship, String phone);

    // Validation Operations
    boolean existsById(UUID id);
    boolean existsByPatientId(UUID patientId);

    // Statistics Operations
    long countByPatientId(UUID patientId);
    long countAll();
    List<Object[]> getRelationshipStatistics();
    List<String> getDistinctRelationships();
    List<UUID> findPatientsWithMultipleContacts();
    List<UUID> findPatientsWithoutEmergencyContacts();

    // Bulk Operations
    List<PatientEmergencyContactDto> createBatch(List<PatientEmergencyContactDto> patientEmergencyContactDtos);
    void deleteByPatientId(UUID patientId);

    // Contact Management
    int getEmergencyContactCount(UUID patientId);
    List<String> getEmergencyContactPhones(UUID patientId);
    boolean hasEmergencyContacts(UUID patientId);
}
