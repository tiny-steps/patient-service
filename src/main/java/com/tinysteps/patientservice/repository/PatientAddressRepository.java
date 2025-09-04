package com.tinysteps.patientservice.repository;

import com.tinysteps.patientservice.model.PatientAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientAddressRepository extends JpaRepository<PatientAddress, UUID> {

    // Find by patient ID
    List<PatientAddress> findByPatientId(UUID patientId);
    Page<PatientAddress> findByPatientId(UUID patientId, Pageable pageable);

    // Find by address ID
    List<PatientAddress> findByAddressId(UUID addressId);
    Page<PatientAddress> findByAddressId(UUID addressId, Pageable pageable);

    // Find by patient and address
    List<PatientAddress> findByPatientIdAndAddressId(UUID patientId, UUID addressId);
    Optional<PatientAddress> findFirstByPatientIdAndAddressId(UUID patientId, UUID addressId);

    // Validation methods
    boolean existsByPatientId(UUID patientId);
    boolean existsByAddressId(UUID addressId);
    boolean existsByPatientIdAndAddressId(UUID patientId, UUID addressId);

    // Count methods
    long countByPatientId(UUID patientId);
    long countByAddressId(UUID addressId);

    // Delete methods
    void deleteByPatientId(UUID patientId);
    void deleteByAddressId(UUID addressId);
    void deleteByPatientIdAndAddressId(UUID patientId, UUID addressId);

    // Statistics methods
    @Query("SELECT COUNT(DISTINCT pa.addressId) FROM PatientAddress pa")
    long countDistinctAddresses();

    @Query("SELECT COUNT(DISTINCT pa.patient.id) FROM PatientAddress pa")
    long countDistinctPatients();

    @Query("SELECT pa.addressId, COUNT(pa) FROM PatientAddress pa GROUP BY pa.addressId ORDER BY COUNT(pa) DESC")
    List<Object[]> getAddressUsageStatistics();

    // Find patients with multiple addresses
    @Query("SELECT pa.patient.id FROM PatientAddress pa GROUP BY pa.patient.id HAVING COUNT(pa) > 1")
    List<UUID> findPatientsWithMultipleAddresses();

    // Find addresses used by multiple patients
    @Query("SELECT pa.addressId FROM PatientAddress pa GROUP BY pa.addressId HAVING COUNT(pa) > 1")
    List<UUID> findAddressesUsedByMultiplePatients();
}
