package com.tintsteps.patientservice.service.impl;

import com.tintsteps.patientservice.dto.PatientAddressDto;
import com.tintsteps.patientservice.exception.PatientNotFoundException;
import com.tintsteps.patientservice.exception.PatientServiceException;
import com.tintsteps.patientservice.mapper.PatientAddressMapper;
import com.tintsteps.patientservice.model.Patient;
import com.tintsteps.patientservice.model.PatientAddress;
import com.tintsteps.patientservice.repository.PatientAddressRepository;
import com.tintsteps.patientservice.repository.PatientRepository;
import com.tintsteps.patientservice.service.PatientAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientAddressServiceImpl implements PatientAddressService {

    private final PatientAddressRepository patientAddressRepository;
    private final PatientRepository patientRepository;
    private final PatientAddressMapper patientAddressMapper = PatientAddressMapper.INSTANCE;

    @Override
    @Transactional
    public PatientAddressDto create(PatientAddressDto patientAddressDto) {
        log.info("Creating patient address for patient ID: {}", patientAddressDto.getPatientId());

        try {
            if (patientAddressDto.getPatientId() == null) {
                throw new IllegalArgumentException("Patient ID is required");
            }

            if (patientAddressDto.getAddressId() == null) {
                throw new IllegalArgumentException("Address ID is required");
            }

            // Verify patient exists
            Patient patient = patientRepository.findById(patientAddressDto.getPatientId())
                    .orElseThrow(() -> new PatientNotFoundException(patientAddressDto.getPatientId()));

            // Check if link already exists
            if (existsByPatientIdAndAddressId(patientAddressDto.getPatientId(), patientAddressDto.getAddressId())) {
                throw new PatientServiceException("Patient is already linked to this address");
            }

            PatientAddress patientAddress = patientAddressMapper.patientAddressDtoToPatientAddress(patientAddressDto);
            patientAddress.setPatient(patient);

            PatientAddress savedAddress = patientAddressRepository.save(patientAddress);

            log.info("Patient address created successfully with ID: {}", savedAddress.getId());
            return patientAddressMapper.patientAddressToPatientAddressDto(savedAddress);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating patient address: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create patient address", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PatientAddressDto findById(UUID id) {
        log.info("Finding patient address by ID: {}", id);

        PatientAddress patientAddress = patientAddressRepository.findById(id)
                .orElseThrow(() -> new PatientServiceException("Patient address not found with id: " + id));

        return patientAddressMapper.patientAddressToPatientAddressDto(patientAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientAddressDto> findByPatientId(UUID patientId) {
        log.info("Finding patient addresses by patient ID: {}", patientId);

        List<PatientAddress> addresses = patientAddressRepository.findByPatientId(patientId);
        return addresses.stream()
                .map(patientAddressMapper::patientAddressToPatientAddressDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientAddressDto> findAll(Pageable pageable) {
        log.info("Finding all patient addresses with pagination");

        Page<PatientAddress> addresses = patientAddressRepository.findAll(pageable);
        return addresses.map(patientAddressMapper::patientAddressToPatientAddressDto);
    }

    @Override
    @Transactional
    public PatientAddressDto update(UUID id, PatientAddressDto patientAddressDto) {
        log.info("Updating patient address with ID: {}", id);

        try {
            PatientAddress existingAddress = patientAddressRepository.findById(id)
                    .orElseThrow(() -> new PatientServiceException("Patient address not found with id: " + id));

            // Update address ID if provided
            if (patientAddressDto.getAddressId() != null) {
                existingAddress.setAddressId(patientAddressDto.getAddressId());
            }

            PatientAddress updatedAddress = patientAddressRepository.save(existingAddress);
            return patientAddressMapper.patientAddressToPatientAddressDto(updatedAddress);
        } catch (Exception e) {
            log.error("Error updating patient address: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to update patient address", e);
        }
    }

    @Override
    @Transactional
    public PatientAddressDto partialUpdate(UUID id, PatientAddressDto patientAddressDto) {
        return update(id, patientAddressDto);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting patient address with ID: {}", id);

        try {
            if (!patientAddressRepository.existsById(id)) {
                throw new PatientServiceException("Patient address not found with id: " + id);
            }

            patientAddressRepository.deleteById(id);
            log.info("Patient address deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting patient address: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete patient address", e);
        }
    }

    // Search Operations
    @Override
    @Transactional(readOnly = true)
    public List<PatientAddressDto> findByAddressId(UUID addressId) {
        log.info("Finding patient addresses by address ID: {}", addressId);

        List<PatientAddress> addresses = patientAddressRepository.findByAddressId(addressId);
        return addresses.stream()
                .map(patientAddressMapper::patientAddressToPatientAddressDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<PatientAddressDto> findByPatientId(UUID patientId, Pageable pageable) {
        log.info("Finding patient addresses by patient ID: {} with pagination", patientId);

        Page<PatientAddress> addresses = patientAddressRepository.findByPatientId(patientId, pageable);
        return addresses.map(patientAddressMapper::patientAddressToPatientAddressDto);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<PatientAddressDto> searchAddresses(UUID patientId, UUID addressId, Pageable pageable) {
        log.info("Searching patient addresses with multiple criteria");

        // For now, implement basic search - can be enhanced with Specifications
        Page<PatientAddress> addresses = patientAddressRepository.findAll(pageable);
        return addresses.map(patientAddressMapper::patientAddressToPatientAddressDto);
    }

    // Business Operations
    @Override
    @Transactional
    public PatientAddressDto linkPatientToAddress(UUID patientId, UUID addressId) {
        log.info("Linking patient ID: {} to address ID: {}", patientId, addressId);

        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(patientId));

            if (existsByPatientIdAndAddressId(patientId, addressId)) {
                throw new PatientServiceException("Patient is already linked to this address");
            }

            PatientAddress patientAddress = new PatientAddress();
            patientAddress.setPatient(patient);
            patientAddress.setAddressId(addressId);

            PatientAddress savedAddress = patientAddressRepository.save(patientAddress);
            return patientAddressMapper.patientAddressToPatientAddressDto(savedAddress);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error linking patient to address: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to link patient to address", e);
        }
    }

    @Override
    @Transactional
    public void unlinkPatientFromAddress(UUID patientId, UUID addressId) {
        log.info("Unlinking patient ID: {} from address ID: {}", patientId, addressId);

        try {
            patientAddressRepository.deleteByPatientIdAndAddressId(patientId, addressId);
        } catch (Exception e) {
            log.error("Error unlinking patient from address: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to unlink patient from address", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPatientLinkedToAddress(UUID patientId, UUID addressId) {
        return patientAddressRepository.existsByPatientIdAndAddressId(patientId, addressId);
    }

    // Validation Operations
    @Transactional(readOnly = true)
    @Override
    public boolean existsById(UUID id) {
        return patientAddressRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByPatientId(UUID patientId) {
        return patientAddressRepository.existsByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByAddressId(UUID addressId) {
        return patientAddressRepository.existsByAddressId(addressId);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByPatientIdAndAddressId(UUID patientId, UUID addressId) {
        return patientAddressRepository.existsByPatientIdAndAddressId(patientId, addressId);
    }

    // Statistics Operations
    @Transactional(readOnly = true)
    @Override
    public long countByPatientId(UUID patientId) {
        return patientAddressRepository.countByPatientId(patientId);
    }


    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return patientAddressRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countDistinctAddresses() {
        return patientAddressRepository.countDistinctAddresses();
    }

    @Override
    @Transactional(readOnly = true)
    public long countDistinctPatients() {
        return patientAddressRepository.countDistinctPatients();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAddressUsageStatistics() {
        return patientAddressRepository.getAddressUsageStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findPatientsWithMultipleAddresses() {
        return patientAddressRepository.findPatientsWithMultipleAddresses();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findAddressesUsedByMultiplePatients() {
        return patientAddressRepository.findAddressesUsedByMultiplePatients();
    }

    // Bulk Operations
    @Override
    @Transactional
    public List<PatientAddressDto> createBatch(List<PatientAddressDto> patientAddressDtos) {
        log.info("Creating batch of {} patient addresses", patientAddressDtos.size());

        try {
            List<PatientAddress> addresses = patientAddressDtos.stream()
                    .map(dto -> {
                        Patient patient = patientRepository.findById(dto.getPatientId())
                                .orElseThrow(() -> new PatientNotFoundException(dto.getPatientId()));
                        PatientAddress address = patientAddressMapper.patientAddressDtoToPatientAddress(dto);
                        address.setPatient(patient);
                        return address;
                    })
                    .collect(Collectors.toList());

            List<PatientAddress> savedAddresses = patientAddressRepository.saveAll(addresses);
            return savedAddresses.stream()
                    .map(patientAddressMapper::patientAddressToPatientAddressDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error creating batch addresses: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create batch addresses", e);
        }
    }

    @Override
    @Transactional
    public void deleteByPatientId(UUID patientId) {
        log.info("Deleting all addresses for patient ID: {}", patientId);
        patientAddressRepository.deleteByPatientId(patientId);
    }

    @Override
    @Transactional
    public void deleteByAddressId(UUID addressId) {
        log.info("Deleting all patient links for address ID: {}", addressId);
        patientAddressRepository.deleteByAddressId(addressId);
    }

    @Transactional
    @Override
    public void deleteBatch(List<UUID> ids) {
        log.info("Deleting batch of {} patient addresses", ids.size());
        patientAddressRepository.deleteAllById(ids);
    }

    // Address Management
    @Override
    @Transactional(readOnly = true)
    public int getAddressCount(UUID patientId) {
        return (int) countByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getAddressIds(UUID patientId) {
        List<PatientAddress> addresses = patientAddressRepository.findByPatientId(patientId);
        return addresses.stream()
                .map(PatientAddress::getAddressId)
                .collect(Collectors.toList());
    }

}
