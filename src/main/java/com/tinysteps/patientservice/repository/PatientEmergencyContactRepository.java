package com.tinysteps.patientservice.repository;

import com.tinysteps.patientservice.model.PatientEmergencyContact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientEmergencyContactRepository extends JpaRepository<PatientEmergencyContact, UUID> {

    // Find by patient ID
    List<PatientEmergencyContact> findByPatientId(UUID patientId);
    Page<PatientEmergencyContact> findByPatientId(UUID patientId, Pageable pageable);

    // Find by name
    List<PatientEmergencyContact> findByNameContainingIgnoreCase(String name);
    Page<PatientEmergencyContact> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Find by relationship
    List<PatientEmergencyContact> findByRelationshipContainingIgnoreCase(String relationship);
    Page<PatientEmergencyContact> findByRelationshipContainingIgnoreCase(String relationship, Pageable pageable);

    // Find by phone
    List<PatientEmergencyContact> findByPhoneContaining(String phone);
    Page<PatientEmergencyContact> findByPhoneContaining(String phone, Pageable pageable);

    // Find by patient and relationship
    List<PatientEmergencyContact> findByPatientIdAndRelationshipContainingIgnoreCase(UUID patientId, String relationship);

    // Validation methods
    boolean existsByPatientId(UUID patientId);
    boolean existsByPhone(String phone);
    boolean existsByPatientIdAndPhone(UUID patientId, String phone);

    // Count methods
    long countByPatientId(UUID patientId);
    long countByRelationshipContainingIgnoreCase(String relationship);

    // Delete methods
    void deleteByPatientId(UUID patientId);

    // Statistics methods
    @Query("SELECT pec.relationship, COUNT(pec) FROM PatientEmergencyContact pec WHERE pec.relationship IS NOT NULL GROUP BY pec.relationship ORDER BY COUNT(pec) DESC")
    List<Object[]> getRelationshipStatistics();

    @Query("SELECT DISTINCT pec.relationship FROM PatientEmergencyContact pec WHERE pec.relationship IS NOT NULL ORDER BY pec.relationship")
    List<String> findDistinctRelationships();

    // Find patients with multiple emergency contacts
    @Query("SELECT pec.patient.id FROM PatientEmergencyContact pec GROUP BY pec.patient.id HAVING COUNT(pec) > 1")
    List<UUID> findPatientsWithMultipleContacts();

    // Find patients without emergency contacts
    @Query("SELECT p.id FROM Patient p WHERE p.id NOT IN (SELECT DISTINCT pec.patient.id FROM PatientEmergencyContact pec)")
    List<UUID> findPatientsWithoutEmergencyContacts();
}
