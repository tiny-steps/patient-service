package com.tintsteps.patientservice.service.impl;

import com.tintsteps.patientservice.dto.PatientInsuranceDto;
import com.tintsteps.patientservice.exception.PatientNotFoundException;
import com.tintsteps.patientservice.exception.PatientServiceException;
import com.tintsteps.patientservice.mapper.PatientInsuranceMapper;
import com.tintsteps.patientservice.model.Patient;
import com.tintsteps.patientservice.model.PatientInsurance;
import com.tintsteps.patientservice.repository.PatientInsuranceRepository;
import com.tintsteps.patientservice.repository.PatientRepository;
import com.tintsteps.patientservice.service.PatientInsuranceService;
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
public class PatientInsuranceServiceImpl implements PatientInsuranceService {

    private final PatientInsuranceRepository patientInsuranceRepository;
    private final PatientRepository patientRepository;
    private final PatientInsuranceMapper patientInsuranceMapper = PatientInsuranceMapper.INSTANCE;

    @Override
    @Transactional
    public PatientInsuranceDto create(PatientInsuranceDto patientInsuranceDto) {
        log.info("Creating insurance for patient ID: {}", patientInsuranceDto.getPatientId());

        try {
            if (patientInsuranceDto.getPatientId() == null) {
                throw new IllegalArgumentException("Patient ID is required");
            }

            // Verify patient exists
            Patient patient = patientRepository.findById(patientInsuranceDto.getPatientId())
                    .orElseThrow(() -> new PatientNotFoundException(patientInsuranceDto.getPatientId()));

            PatientInsurance insurance = patientInsuranceMapper.patientInsuranceDtoToPatientInsurance(patientInsuranceDto);
            insurance.setPatient(patient);

            PatientInsurance savedInsurance = patientInsuranceRepository.save(insurance);

            log.info("Insurance created successfully with ID: {}", savedInsurance.getId());
            return patientInsuranceMapper.patientInsuranceToPatientInsuranceDto(savedInsurance);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating insurance: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create insurance", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PatientInsuranceDto findById(UUID id) {
        log.info("Finding insurance by ID: {}", id);

        PatientInsurance insurance = patientInsuranceRepository.findById(id)
                .orElseThrow(() -> new PatientServiceException("Insurance not found with id: " + id));

        return patientInsuranceMapper.patientInsuranceToPatientInsuranceDto(insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceDto> findByPatientId(UUID patientId) {
        log.info("Finding insurance by patient ID: {}", patientId);

        List<PatientInsurance> insurances = patientInsuranceRepository.findByPatientId(patientId);
        return insurances.stream()
                .map(patientInsuranceMapper::patientInsuranceToPatientInsuranceDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientInsuranceDto> findAll(Pageable pageable) {
        log.info("Finding all insurance with pagination");

        Page<PatientInsurance> insurances = patientInsuranceRepository.findAll(pageable);
        return insurances.map(patientInsuranceMapper::patientInsuranceToPatientInsuranceDto);
    }

    @Override
    @Transactional
    public PatientInsuranceDto update(UUID id, PatientInsuranceDto patientInsuranceDto) {
        log.info("Updating insurance with ID: {}", id);

        try {
            PatientInsurance existingInsurance = patientInsuranceRepository.findById(id)
                    .orElseThrow(() -> new PatientServiceException("Insurance not found with id: " + id));

            // Update fields
            if (patientInsuranceDto.getProvider() != null) {
                existingInsurance.setProvider(patientInsuranceDto.getProvider());
            }
            if (patientInsuranceDto.getPolicyNumber() != null) {
                existingInsurance.setPolicyNumber(patientInsuranceDto.getPolicyNumber());
            }
            if (patientInsuranceDto.getCoverageDetails() != null) {
                existingInsurance.setCoverageDetails(patientInsuranceDto.getCoverageDetails());
            }

            PatientInsurance updatedInsurance = patientInsuranceRepository.save(existingInsurance);
            return patientInsuranceMapper.patientInsuranceToPatientInsuranceDto(updatedInsurance);
        } catch (Exception e) {
            log.error("Error updating insurance: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to update insurance", e);
        }
    }

    @Override
    @Transactional
    public PatientInsuranceDto partialUpdate(UUID id, PatientInsuranceDto patientInsuranceDto) {
        return update(id, patientInsuranceDto);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting insurance with ID: {}", id);

        try {
            if (!patientInsuranceRepository.existsById(id)) {
                throw new PatientServiceException("Insurance not found with id: " + id);
            }

            patientInsuranceRepository.deleteById(id);
            log.info("Insurance deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting insurance: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete insurance", e);
        }
    }

    // Search Operations
    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceDto> findByProvider(String provider) {
        List<PatientInsurance> insurances = patientInsuranceRepository.findByProviderContainingIgnoreCase(provider);
        return insurances.stream()
                .map(patientInsuranceMapper::patientInsuranceToPatientInsuranceDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientInsuranceDto> findByProvider(String provider, Pageable pageable) {
        Page<PatientInsurance> insurances = patientInsuranceRepository.findByProviderContainingIgnoreCase(provider, pageable);
        return insurances.map(patientInsuranceMapper::patientInsuranceToPatientInsuranceDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientInsuranceDto> searchInsurance(UUID patientId, String provider, String policyNumber, String coverageDetails, Pageable pageable) {
        // For now, implement basic search - can be enhanced with Specifications
        Page<PatientInsurance> insurances = patientInsuranceRepository.findAll(pageable);
        return insurances.map(patientInsuranceMapper::patientInsuranceToPatientInsuranceDto);
    }

    // Business Operations
    @Override
    @Transactional
    public PatientInsuranceDto addInsurance(UUID patientId, String provider, String policyNumber, String coverageDetails) {
        log.info("Adding insurance for patient ID: {}", patientId);

        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(patientId));

            PatientInsurance insurance = new PatientInsurance();
            insurance.setPatient(patient);
            insurance.setProvider(provider);
            insurance.setPolicyNumber(policyNumber);
            insurance.setCoverageDetails(coverageDetails);

            PatientInsurance savedInsurance = patientInsuranceRepository.save(insurance);
            return patientInsuranceMapper.patientInsuranceToPatientInsuranceDto(savedInsurance);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding insurance: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to add insurance", e);
        }
    }

    @Override
    @Transactional
    public void removeInsurance(UUID patientId, String policyNumber) {
        log.info("Removing insurance for patient ID: {} with policy: {}", patientId, policyNumber);

        List<PatientInsurance> insurances = patientInsuranceRepository.findByPatientId(patientId);
        List<PatientInsurance> insurancesToDelete = insurances.stream()
                .filter(insurance -> insurance.getPolicyNumber().equals(policyNumber))
                .collect(Collectors.toList());

        if (!insurancesToDelete.isEmpty()) {
            patientInsuranceRepository.deleteAll(insurancesToDelete);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasInsurance(UUID patientId, String policyNumber) {
        return patientInsuranceRepository.existsByPatientIdAndPolicyNumber(patientId, policyNumber);
    }

    @Override
    @Transactional
    public PatientInsuranceDto updateInsuranceInfo(UUID id, String provider, String policyNumber, String coverageDetails) {
        PatientInsuranceDto dto = new PatientInsuranceDto();
        dto.setProvider(provider);
        dto.setPolicyNumber(policyNumber);
        dto.setCoverageDetails(coverageDetails);
        return update(id, dto);
    }

    // Validation Operations
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return patientInsuranceRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByPatientId(UUID patientId) {
        return patientInsuranceRepository.existsByPatientId(patientId);
    }



    // Statistics Operations

    @Transactional(readOnly = true)
    public long countByPatientId(UUID patientId) {
        return patientInsuranceRepository.countByPatientId(patientId);
    }


    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return patientInsuranceRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getProviderStatistics() {
        return patientInsuranceRepository.getProviderStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctProviders() {
        return patientInsuranceRepository.findDistinctProviders();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findPatientsWithMultipleInsurances() {
        return patientInsuranceRepository.findPatientsWithMultipleInsurances();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findPatientsWithoutInsurance() {
        return patientInsuranceRepository.findPatientsWithoutInsurance();
    }

    // Bulk Operations
    @Override
    @Transactional
    public List<PatientInsuranceDto> createBatch(List<PatientInsuranceDto> patientInsuranceDtos) {
        log.info("Creating batch of {} insurances", patientInsuranceDtos.size());

        try {
            List<PatientInsurance> insurances = patientInsuranceDtos.stream()
                    .map(dto -> {
                        Patient patient = patientRepository.findById(dto.getPatientId())
                                .orElseThrow(() -> new PatientNotFoundException(dto.getPatientId()));
                        PatientInsurance insurance = patientInsuranceMapper.patientInsuranceDtoToPatientInsurance(dto);
                        insurance.setPatient(patient);
                        return insurance;
                    })
                    .collect(Collectors.toList());

            List<PatientInsurance> savedInsurances = patientInsuranceRepository.saveAll(insurances);
            return savedInsurances.stream()
                    .map(patientInsuranceMapper::patientInsuranceToPatientInsuranceDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error creating batch insurances: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create batch insurances", e);
        }
    }

    @Override
    @Transactional
    public void deleteByPatientId(UUID patientId) {
        log.info("Deleting all insurances for patient ID: {}", patientId);
        patientInsuranceRepository.deleteByPatientId(patientId);
    }

    // Insurance Management
    @Override
    @Transactional(readOnly = true)
    public int getInsuranceCount(UUID patientId) {
        return (int) countByPatientId(patientId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<String> getInsuranceProviders(UUID patientId) {
        List<PatientInsurance> insurances = patientInsuranceRepository.findByPatientId(patientId);
        return insurances.stream()
                .map(PatientInsurance::getProvider)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasInsurance(UUID patientId) {
        return existsByPatientId(patientId);
    }

}
