package com.tinysteps.patientservice.service;

import com.tinysteps.patientservice.dto.PatientInsuranceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PatientInsuranceService {

    // CRUD Operations
    PatientInsuranceDto create(PatientInsuranceDto patientInsuranceDto);
    PatientInsuranceDto findById(UUID id);
    List<PatientInsuranceDto> findByPatientId(UUID patientId);
    Page<PatientInsuranceDto> findAll(Pageable pageable);
    PatientInsuranceDto update(UUID id, PatientInsuranceDto patientInsuranceDto);
    PatientInsuranceDto partialUpdate(UUID id, PatientInsuranceDto patientInsuranceDto);
    void delete(UUID id);

    // Search Operations - Only keep used methods
    Page<PatientInsuranceDto> findByProvider(String provider,Pageable pageable);

    List<PatientInsuranceDto> findByProvider(String provider);
    Page<PatientInsuranceDto> searchInsurance(UUID patientId, String provider, String policyNumber,
                                             String coverageDetails, Pageable pageable);

    // Business Operations - Only keep used methods
    PatientInsuranceDto addInsurance(UUID patientId, String provider, String policyNumber, String coverageDetails);
    void removeInsurance(UUID patientId, String policyNumber);
    boolean hasInsurance(UUID patientId, String policyNumber);
    PatientInsuranceDto updateInsuranceInfo(UUID id, String provider, String policyNumber, String coverageDetails);

    // Statistics Operations - Only keep used methods
    long countAll();
    List<Object[]> getProviderStatistics();
    List<String> getDistinctProviders();
    List<UUID> findPatientsWithoutInsurance();

    // Bulk Operations - Only keep used methods
    List<PatientInsuranceDto> createBatch(List<PatientInsuranceDto> patientInsuranceDtos);
    void deleteByPatientId(UUID patientId);

    // Insurance Management - Only keep used methods
    boolean hasInsurance(UUID patientId);

    int getInsuranceCount(UUID patientId);

    List<String> getInsuranceProviders(UUID patientId);

    List<UUID> findPatientsWithMultipleInsurances();
}
