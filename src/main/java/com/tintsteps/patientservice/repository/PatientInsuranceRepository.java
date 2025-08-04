package com.tintsteps.patientservice.repository;

import com.tintsteps.patientservice.model.PatientInsurance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, UUID> {

    // Find by patient ID
    List<PatientInsurance> findByPatientId(UUID patientId);
    Page<PatientInsurance> findByPatientId(UUID patientId, Pageable pageable);

    // Find by provider
    List<PatientInsurance> findByProviderContainingIgnoreCase(String provider);
    Page<PatientInsurance> findByProviderContainingIgnoreCase(String provider, Pageable pageable);

    // Find by policy number
    List<PatientInsurance> findByPolicyNumberContainingIgnoreCase(String policyNumber);
    Page<PatientInsurance> findByPolicyNumberContainingIgnoreCase(String policyNumber, Pageable pageable);

    // Find by coverage details
    List<PatientInsurance> findByCoverageDetailsContainingIgnoreCase(String coverageDetails);
    Page<PatientInsurance> findByCoverageDetailsContainingIgnoreCase(String coverageDetails, Pageable pageable);

    // Find by patient and provider
    List<PatientInsurance> findByPatientIdAndProviderContainingIgnoreCase(UUID patientId, String provider);

    // Validation methods
    boolean existsByPatientId(UUID patientId);
    boolean existsByPolicyNumber(String policyNumber);
    boolean existsByPatientIdAndPolicyNumber(UUID patientId, String policyNumber);

    // Count methods
    long countByPatientId(UUID patientId);
    long countByProviderContainingIgnoreCase(String provider);

    // Delete methods
    void deleteByPatientId(UUID patientId);

    // Statistics methods
    @Query("SELECT pi.provider, COUNT(pi) FROM PatientInsurance pi WHERE pi.provider IS NOT NULL GROUP BY pi.provider ORDER BY COUNT(pi) DESC")
    List<Object[]> getProviderStatistics();

    @Query("SELECT DISTINCT pi.provider FROM PatientInsurance pi WHERE pi.provider IS NOT NULL ORDER BY pi.provider")
    List<String> findDistinctProviders();

    // Find patients with multiple insurance policies
    @Query("SELECT pi.patient.id FROM PatientInsurance pi GROUP BY pi.patient.id HAVING COUNT(pi) > 1")
    List<UUID> findPatientsWithMultipleInsurances();

    // Find patients without insurance
    @Query("SELECT p.id FROM Patient p WHERE p.id NOT IN (SELECT DISTINCT pi.patient.id FROM PatientInsurance pi)")
    List<UUID> findPatientsWithoutInsurance();
}
