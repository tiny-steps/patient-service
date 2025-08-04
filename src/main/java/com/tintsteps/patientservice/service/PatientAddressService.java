package com.tintsteps.patientservice.service;

import com.tintsteps.patientservice.dto.PatientAddressDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PatientAddressService {

    // CRUD Operations
    PatientAddressDto create(PatientAddressDto patientAddressDto);
    PatientAddressDto findById(UUID id);
    List<PatientAddressDto> findByPatientId(UUID patientId);
    Page<PatientAddressDto> findAll(Pageable pageable);
    PatientAddressDto update(UUID id, PatientAddressDto patientAddressDto);
    PatientAddressDto partialUpdate(UUID id, PatientAddressDto patientAddressDto);
    void delete(UUID id);

    // Search Operations - Only keep used methods
    List<PatientAddressDto> findByAddressId(UUID addressId);
    Page<PatientAddressDto> searchAddresses(UUID patientId, UUID addressId, Pageable pageable);

    // Business Operations - Only keep used methods
    PatientAddressDto linkPatientToAddress(UUID patientId, UUID addressId);
    void unlinkPatientFromAddress(UUID patientId, UUID addressId);
    boolean isPatientLinkedToAddress(UUID patientId, UUID addressId);

    // Statistics Operations - Only keep used methods
    long countAll();
    List<Object[]> getAddressUsageStatistics();

    // Bulk Operations - Only keep used methods
    List<PatientAddressDto> createBatch(List<PatientAddressDto> patientAddressDtos);
    void deleteByPatientId(UUID patientId);
}
