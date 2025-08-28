package com.tintsteps.patientservice.service.impl;

import com.tintsteps.patientservice.dto.PatientAllergyDto;
import com.tintsteps.patientservice.exception.PatientNotFoundException;
import com.tintsteps.patientservice.exception.PatientServiceException;
import com.tintsteps.patientservice.mapper.PatientAllergyMapper;
import com.tintsteps.patientservice.model.Patient;
import com.tintsteps.patientservice.model.PatientAllergy;
import com.tintsteps.patientservice.repository.PatientAllergyRepository;
import com.tintsteps.patientservice.repository.PatientRepository;
import com.tintsteps.patientservice.service.PatientAllergyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientAllergyServiceImpl implements PatientAllergyService {

    private final PatientAllergyRepository patientAllergyRepository;
    private final PatientRepository patientRepository;
    private final PatientAllergyMapper patientAllergyMapper = PatientAllergyMapper.INSTANCE;

    // Critical allergies that require special attention
    private static final List<String> CRITICAL_ALLERGENS = Arrays.asList(
            "penicillin", "peanuts", "shellfish", "latex", "bee venom", "eggs", "milk");

    @Override
    @Transactional
    @CacheEvict(value = { "patient-allergies", "patients" }, allEntries = true)
    public PatientAllergyDto create(PatientAllergyDto patientAllergyDto) {
        log.info("Creating patient allergy for patient ID: {}", patientAllergyDto.getPatientId());

        try {
            if (patientAllergyDto.getPatientId() == null) {
                throw new IllegalArgumentException("Patient ID is required");
            }

            if (patientAllergyDto.getAllergen() == null || patientAllergyDto.getAllergen().trim().isEmpty()) {
                throw new IllegalArgumentException("Allergen is required");
            }

            // Verify patient exists
            if (!patientRepository.existsById(patientAllergyDto.getPatientId())) {
                throw new PatientNotFoundException(patientAllergyDto.getPatientId());
            }

            PatientAllergy patientAllergy = patientAllergyMapper.patientAllergyDtoToPatientAllergy(patientAllergyDto);
            PatientAllergy savedAllergy = patientAllergyRepository.save(patientAllergy);

            log.info("Patient allergy created successfully with ID: {}", savedAllergy.getId());
            return patientAllergyMapper.patientAllergyToPatientAllergyDto(savedAllergy);
        } catch (Exception e) {
            log.error("Error creating patient allergy: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create patient allergy", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patient-allergies", key = "#id")
    public PatientAllergyDto findById(UUID id) {
        log.info("Finding patient allergy by ID: {}", id);

        PatientAllergy patientAllergy = patientAllergyRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("PatientAllergy", "id", id));

        return patientAllergyMapper.patientAllergyToPatientAllergyDto(patientAllergy);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientAllergyDto> findAll(Pageable pageable) {
        log.info("Finding all patient allergies with pagination: {}", pageable);

        Page<PatientAllergy> allergies = patientAllergyRepository.findAll(pageable);
        return allergies.map(patientAllergyMapper::patientAllergyToPatientAllergyDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "patient-allergies", "patients" }, key = "#id")
    public PatientAllergyDto update(UUID id, PatientAllergyDto patientAllergyDto) {
        log.info("Updating patient allergy with ID: {}", id);

        try {
            PatientAllergy existingAllergy = patientAllergyRepository.findById(id)
                    .orElseThrow(() -> new PatientNotFoundException("PatientAllergy", "id", id));

            // Update fields
            if (patientAllergyDto.getAllergen() != null) {
                existingAllergy.setAllergen(patientAllergyDto.getAllergen());
            }
            if (patientAllergyDto.getReaction() != null) {
                existingAllergy.setReaction(patientAllergyDto.getReaction());
            }

            PatientAllergy updatedAllergy = patientAllergyRepository.save(existingAllergy);

            log.info("Patient allergy updated successfully with ID: {}", updatedAllergy.getId());
            return patientAllergyMapper.patientAllergyToPatientAllergyDto(updatedAllergy);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating patient allergy: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to update patient allergy", e);
        }
    }

    @Override
    @Transactional
    public PatientAllergyDto partialUpdate(UUID id, PatientAllergyDto patientAllergyDto) {
        return update(id, patientAllergyDto); // Same implementation for now
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting patient allergy with ID: {}", id);

        try {
            if (!existsById(id)) {
                throw new PatientNotFoundException("PatientAllergy", "id", id);
            }

            patientAllergyRepository.deleteById(id);
            log.info("Patient allergy deleted successfully with ID: {}", id);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting patient allergy: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete patient allergy", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patient-allergies", key = "'patient-' + #patientId")
    public List<PatientAllergyDto> findByPatientId(UUID patientId) {
        log.info("Finding allergies for patient ID: {}", patientId);

        List<PatientAllergy> allergies = patientAllergyRepository.findByPatientId(patientId);
        return allergies.stream()
                .map(patientAllergyMapper::patientAllergyToPatientAllergyDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientAllergyDto> findByPatientId(UUID patientId, Pageable pageable) {
        log.info("Finding allergies for patient ID: {} with pagination", patientId);

        Page<PatientAllergy> allergies = patientAllergyRepository.findByPatientId(patientId, pageable);
        return allergies.map(patientAllergyMapper::patientAllergyToPatientAllergyDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientAllergyDto> findByAllergen(String allergen) {
        log.info("Finding allergies by allergen: {}", allergen);

        List<PatientAllergy> allergies = patientAllergyRepository.findByAllergenContainingIgnoreCase(allergen);
        return allergies.stream()
                .map(patientAllergyMapper::patientAllergyToPatientAllergyDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientAllergyDto> findByAllergen(String allergen, Pageable pageable) {
        log.info("Finding allergies by allergen: {} with pagination", allergen);

        Page<PatientAllergy> allergies = patientAllergyRepository.findByAllergenContainingIgnoreCase(allergen,
                pageable);
        return allergies.map(patientAllergyMapper::patientAllergyToPatientAllergyDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "patient-allergies", "patients" }, allEntries = true)
    public PatientAllergyDto addAllergy(UUID patientId, String allergen, String reaction) {
        log.info("Adding allergy for patient ID: {}, allergen: {}", patientId, allergen);

        try {
            // Verify patient exists
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(patientId));

            PatientAllergy patientAllergy = new PatientAllergy();
            patientAllergy.setPatient(patient);
            patientAllergy.setAllergen(allergen);
            patientAllergy.setReaction(reaction);

            PatientAllergy savedAllergy = patientAllergyRepository.save(patientAllergy);
            return patientAllergyMapper.patientAllergyToPatientAllergyDto(savedAllergy);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding allergy: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to add allergy", e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "patient-allergies", "patients" }, allEntries = true)
    public void removeAllergy(UUID patientId, String allergen) {
        log.info("Removing allergy for patient ID: {}, allergen: {}", patientId, allergen);

        try {
            List<PatientAllergy> allergies = patientAllergyRepository
                    .findByPatientIdAndAllergenContainingIgnoreCase(patientId, allergen);
            if (!allergies.isEmpty()) {
                patientAllergyRepository.deleteAll(allergies);
                log.info("Removed {} allergy records for patient ID: {}", allergies.size(), patientId);
            }
        } catch (Exception e) {
            log.error("Error removing allergy: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to remove allergy", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAllergy(UUID patientId, String allergen) {
        return patientAllergyRepository.existsByPatientIdAndAllergen(patientId, allergen);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return patientAllergyRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPatientId(UUID patientId) {
        return patientAllergyRepository.existsByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByPatientId(UUID patientId) {
        return patientAllergyRepository.countByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return patientAllergyRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAllergenStatistics() {
        return patientAllergyRepository.getAllergenStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctAllergens() {
        return patientAllergyRepository.findDistinctAllergens();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctReactions() {
        return patientAllergyRepository.findDistinctReactions();
    }

    @Override
    @Transactional
    public List<PatientAllergyDto> createBatch(List<PatientAllergyDto> patientAllergyDtos) {
        log.info("Creating batch of {} patient allergies", patientAllergyDtos.size());

        try {
            List<PatientAllergy> allergies = patientAllergyDtos.stream()
                    .map(patientAllergyMapper::patientAllergyDtoToPatientAllergy)
                    .collect(Collectors.toList());

            List<PatientAllergy> savedAllergies = patientAllergyRepository.saveAll(allergies);

            return savedAllergies.stream()
                    .map(patientAllergyMapper::patientAllergyToPatientAllergyDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error creating batch of patient allergies: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create batch of patient allergies", e);
        }
    }

    @Override
    @Transactional
    public void deleteByPatientId(UUID patientId) {
        log.info("Deleting all allergies for patient ID: {}", patientId);

        try {
            patientAllergyRepository.deleteByPatientId(patientId);
        } catch (Exception e) {
            log.error("Error deleting allergies for patient: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete allergies for patient", e);
        }
    }

    @Override
    @Transactional
    public void deleteBatch(List<UUID> ids) {
        log.info("Deleting batch of {} patient allergies", ids.size());

        try {
            patientAllergyRepository.deleteAllById(ids);
        } catch (Exception e) {
            log.error("Error deleting batch of patient allergies: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete batch of patient allergies", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCriticalAllergies(UUID patientId) {
        log.info("Getting critical allergies for patient ID: {}", patientId);

        List<PatientAllergy> allergies = patientAllergyRepository.findByPatientId(patientId);

        return allergies.stream()
                .map(PatientAllergy::getAllergen)
                .filter(allergen -> CRITICAL_ALLERGENS.stream()
                        .anyMatch(critical -> allergen.toLowerCase().contains(critical.toLowerCase())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasCriticalAllergies(UUID patientId) {
        return !getCriticalAllergies(patientId).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public int getAllergyCount(UUID patientId) {
        return (int) countByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllergens(UUID patientId) {
        List<PatientAllergy> allergies = patientAllergyRepository.findByPatientId(patientId);
        return allergies.stream()
                .map(PatientAllergy::getAllergen)
                .collect(Collectors.toList());
    }

}
