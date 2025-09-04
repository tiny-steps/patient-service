package com.tinysteps.patientservice.service.impl;

import com.tinysteps.patientservice.dto.PatientMedicationDto;
import com.tinysteps.patientservice.exception.PatientNotFoundException;
import com.tinysteps.patientservice.exception.PatientServiceException;
import com.tinysteps.patientservice.mapper.PatientMedicationMapper;
import com.tinysteps.patientservice.model.Patient;
import com.tinysteps.patientservice.model.PatientMedication;
import com.tinysteps.patientservice.repository.PatientMedicationRepository;
import com.tinysteps.patientservice.repository.PatientRepository;
import com.tinysteps.patientservice.service.PatientMedicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientMedicationServiceImpl implements PatientMedicationService {

    private final PatientMedicationRepository patientMedicationRepository;
    private final PatientRepository patientRepository;
    private final PatientMedicationMapper patientMedicationMapper = PatientMedicationMapper.INSTANCE;

    @Override
    @Transactional
    @CacheEvict(value = { "patient-medications", "patients" }, allEntries = true)
    public PatientMedicationDto create(PatientMedicationDto patientMedicationDto) {
        log.info("Creating patient medication for patient ID: {}", patientMedicationDto.getPatientId());

        try {
            if (patientMedicationDto.getPatientId() == null) {
                throw new IllegalArgumentException("Patient ID is required");
            }

            // Verify patient exists
            Patient patient = patientRepository.findById(patientMedicationDto.getPatientId())
                    .orElseThrow(() -> new PatientNotFoundException(patientMedicationDto.getPatientId()));

            PatientMedication patientMedication = patientMedicationMapper
                    .patientMedicationDtoToPatientMedication(patientMedicationDto);
            patientMedication.setPatient(patient);

            PatientMedication savedMedication = patientMedicationRepository.save(patientMedication);

            log.info("Patient medication created successfully with ID: {}", savedMedication.getId());
            return patientMedicationMapper.patientMedicationToPatientMedicationDto(savedMedication);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating patient medication: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create patient medication", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patient-medications", key = "#id")
    public PatientMedicationDto findById(UUID id) {
        log.info("Finding patient medication by ID: {}", id);

        PatientMedication patientMedication = patientMedicationRepository.findById(id)
                .orElseThrow(() -> new PatientServiceException("Patient medication not found with id: " + id));

        return patientMedicationMapper.patientMedicationToPatientMedicationDto(patientMedication);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patient-medications", key = "'patient-' + #patientId")
    public List<PatientMedicationDto> findByPatientId(UUID patientId) {
        log.info("Finding patient medications by patient ID: {}", patientId);

        List<PatientMedication> medications = patientMedicationRepository.findByPatientId(patientId);
        return medications.stream()
                .map(patientMedicationMapper::patientMedicationToPatientMedicationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicationDto> findAll(Pageable pageable) {
        log.info("Finding all patient medications with pagination");

        Page<PatientMedication> medications = patientMedicationRepository.findAll(pageable);
        return medications.map(patientMedicationMapper::patientMedicationToPatientMedicationDto);
    }

    @Override
    @Transactional
    public PatientMedicationDto update(UUID id, PatientMedicationDto patientMedicationDto) {
        log.info("Updating patient medication with ID: {}", id);

        try {
            PatientMedication existingMedication = patientMedicationRepository.findById(id)
                    .orElseThrow(() -> new PatientServiceException("Patient medication not found with id: " + id));

            // Update fields
            if (patientMedicationDto.getMedicationName() != null) {
                existingMedication.setMedicationName(patientMedicationDto.getMedicationName());
            }
            if (patientMedicationDto.getDosage() != null) {
                existingMedication.setDosage(patientMedicationDto.getDosage());
            }
            if (patientMedicationDto.getStartDate() != null) {
                existingMedication.setStartDate(patientMedicationDto.getStartDate());
            }
            if (patientMedicationDto.getEndDate() != null) {
                existingMedication.setEndDate(patientMedicationDto.getEndDate());
            }

            PatientMedication updatedMedication = patientMedicationRepository.save(existingMedication);
            return patientMedicationMapper.patientMedicationToPatientMedicationDto(updatedMedication);
        } catch (Exception e) {
            log.error("Error updating patient medication: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to update patient medication", e);
        }
    }

    @Override
    @Transactional
    public PatientMedicationDto partialUpdate(UUID id, PatientMedicationDto patientMedicationDto) {
        return update(id, patientMedicationDto);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting patient medication with ID: {}", id);

        try {
            if (!patientMedicationRepository.existsById(id)) {
                throw new PatientServiceException("Patient medication not found with id: " + id);
            }

            patientMedicationRepository.deleteById(id);
            log.info("Patient medication deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting patient medication: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete patient medication", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicationDto> findByMedicationName(String medicationName) {
        log.info("Finding patient medications by medication name: {}", medicationName);

        List<PatientMedication> medications = patientMedicationRepository
                .findByMedicationNameContainingIgnoreCase(medicationName);
        return medications.stream()
                .map(patientMedicationMapper::patientMedicationToPatientMedicationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicationDto> findByMedicationName(String medicationName, Pageable pageable) {
        log.info("Finding patient medications by medication name: {} with pagination", medicationName);

        Page<PatientMedication> medications = patientMedicationRepository
                .findByMedicationNameContainingIgnoreCase(medicationName, pageable);
        return medications.map(patientMedicationMapper::patientMedicationToPatientMedicationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicationDto> findByDosage(String dosage, Pageable pageable) {
        log.info("Finding patient medications by dosage: {} with pagination", dosage);

        Page<PatientMedication> medications = patientMedicationRepository.findByDosageContainingIgnoreCase(dosage,
                pageable);
        return medications.map(patientMedicationMapper::patientMedicationToPatientMedicationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicationDto> findByPatientId(UUID patientId, Pageable pageable) {
        log.info("Finding patient medications by patient ID: {} with pagination", patientId);

        Page<PatientMedication> medications = patientMedicationRepository.findByPatientId(patientId, pageable);
        return medications.map(patientMedicationMapper::patientMedicationToPatientMedicationDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicationDto> searchMedications(UUID patientId, String medicationName, String dosage,
            Date startDate, Date endDate, Pageable pageable) {
        log.info("Searching patient medications with multiple criteria");

        // For now, implement basic search - can be enhanced with Specifications
        Page<PatientMedication> medications = patientMedicationRepository.findAll(pageable);
        return medications.map(patientMedicationMapper::patientMedicationToPatientMedicationDto);
    }

    // Business Operations
    @Override
    @Transactional
    public PatientMedicationDto addMedication(UUID patientId, String medicationName, String dosage, Date startDate,
            Date endDate) {
        log.info("Adding medication for patient ID: {}, medication: {}", patientId, medicationName);

        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(patientId));

            PatientMedication medication = new PatientMedication();
            medication.setPatient(patient);
            medication.setMedicationName(medicationName);
            medication.setDosage(dosage);
            medication.setStartDate(startDate);
            medication.setEndDate(endDate);

            PatientMedication savedMedication = patientMedicationRepository.save(medication);
            return patientMedicationMapper.patientMedicationToPatientMedicationDto(savedMedication);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding medication: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to add medication", e);
        }
    }

    @Override
    @Transactional
    public PatientMedicationDto startMedication(UUID patientId, String medicationName, String dosage) {
        return addMedication(patientId, medicationName, dosage, new Date(), null);
    }

    @Override
    @Transactional
    public PatientMedicationDto stopMedication(UUID patientId, String medicationName, Date endDate) {
        log.info("Stopping medication for patient ID: {}, medication: {}", patientId, medicationName);

        try {
            List<PatientMedication> medications = patientMedicationRepository
                    .findByPatientIdAndMedicationNameContainingIgnoreCase(patientId, medicationName);

            if (medications.isEmpty()) {
                throw new PatientServiceException("No active medication found for patient");
            }

            // Find current medication (no end date or future end date)
            PatientMedication currentMedication = medications.stream()
                    .filter(med -> med.getEndDate() == null || med.getEndDate().after(new Date()))
                    .findFirst()
                    .orElseThrow(() -> new PatientServiceException("No active medication found"));

            currentMedication.setEndDate(endDate);
            PatientMedication updatedMedication = patientMedicationRepository.save(currentMedication);

            return patientMedicationMapper.patientMedicationToPatientMedicationDto(updatedMedication);
        } catch (Exception e) {
            log.error("Error stopping medication: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to stop medication", e);
        }
    }

    @Override
    @Transactional
    public void discontinueMedication(UUID patientId, String medicationName) {
        stopMedication(patientId, medicationName, new Date());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicationDto> getCurrentMedicationsForPatient(UUID patientId) {
        log.info("Getting current medications for patient ID: {}", patientId);

        List<PatientMedication> medications = patientMedicationRepository.findCurrentMedicationsByPatientId(patientId);
        return medications.stream()
                .map(patientMedicationMapper::patientMedicationToPatientMedicationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOnMedication(UUID patientId, String medicationName) {
        return patientMedicationRepository.existsByPatientIdAndMedicationName(patientId, medicationName);
    }

    // Validation Operations
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return patientMedicationRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByPatientId(UUID patientId) {
        return patientMedicationRepository.existsByPatientId(patientId);
    }

    // Statistics Operations
    @Transactional(readOnly = true)
    public long countByPatientId(UUID patientId) {
        return patientMedicationRepository.countByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public long countCurrentMedications(UUID patientId) {
        return patientMedicationRepository.countCurrentMedicationsByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return patientMedicationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMedicationStatistics() {
        return patientMedicationRepository.getMedicationStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctMedicationNames() {
        return patientMedicationRepository.findDistinctMedicationNames();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctDosages() {
        return patientMedicationRepository.findDistinctDosages();
    }

    // Bulk Operations
    @Override
    @Transactional
    public List<PatientMedicationDto> createBatch(List<PatientMedicationDto> patientMedicationDtos) {
        log.info("Creating batch of {} patient medications", patientMedicationDtos.size());

        try {
            List<PatientMedication> medications = patientMedicationDtos.stream()
                    .map(dto -> {
                        Patient patient = patientRepository.findById(dto.getPatientId())
                                .orElseThrow(() -> new PatientNotFoundException(dto.getPatientId()));
                        PatientMedication medication = patientMedicationMapper
                                .patientMedicationDtoToPatientMedication(dto);
                        medication.setPatient(patient);
                        return medication;
                    })
                    .collect(Collectors.toList());

            List<PatientMedication> savedMedications = patientMedicationRepository.saveAll(medications);
            return savedMedications.stream()
                    .map(patientMedicationMapper::patientMedicationToPatientMedicationDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error creating batch medications: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create batch medications", e);
        }
    }

    @Override
    @Transactional
    public void deleteByPatientId(UUID patientId) {
        log.info("Deleting all medications for patient ID: {}", patientId);
        patientMedicationRepository.deleteByPatientId(patientId);
    }

    @Transactional
    public void deleteBatch(List<UUID> ids) {
        log.info("Deleting batch of {} medications", ids.size());
        patientMedicationRepository.deleteAllById(ids);
    }

    // Medical Operations
    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveMedicationNames(UUID patientId) {
        log.info("Getting active medication names for patient ID: {}", patientId);

        List<PatientMedication> medications = patientMedicationRepository.findCurrentMedicationsByPatientId(patientId);
        return medications.stream()
                .map(PatientMedication::getMedicationName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public int getActiveMedicationCount(UUID patientId) {
        return (int) countCurrentMedications(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicationDto> getMedicationHistory(UUID patientId) {
        return findByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMedicationConflicts(UUID patientId, String newMedication) {
        // This is a simplified implementation - in real scenarios, you'd check against
        // a drug interaction database
        log.info("Checking medication conflicts for patient ID: {} and medication: {}", patientId, newMedication);

        List<String> currentMedications = getActiveMedicationNames(patientId);
        // For now, just check if the same medication is already being taken
        return currentMedications.stream()
                .anyMatch(med -> med.equalsIgnoreCase(newMedication));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPotentialInteractions(UUID patientId, String medicationName) {
        // This is a simplified implementation - in real scenarios, you'd check against
        // a drug interaction database
        log.info("Getting potential interactions for patient ID: {} and medication: {}", patientId, medicationName);

        // Return empty list for now - would be populated with actual drug interaction
        // logic
        return List.of();
    }

    // Medication Management
    @Override
    @Transactional
    public PatientMedicationDto renewMedication(UUID medicationId, Date newEndDate) {
        log.info("Renewing medication with ID: {}", medicationId);

        try {
            PatientMedication medication = patientMedicationRepository.findById(medicationId)
                    .orElseThrow(() -> new PatientServiceException("Medication not found with id: " + medicationId));

            medication.setEndDate(newEndDate);
            PatientMedication updatedMedication = patientMedicationRepository.save(medication);

            return patientMedicationMapper.patientMedicationToPatientMedicationDto(updatedMedication);
        } catch (Exception e) {
            log.error("Error renewing medication: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to renew medication", e);
        }
    }

    @Override
    @Transactional
    public PatientMedicationDto changeDosage(UUID medicationId, String newDosage) {
        log.info("Changing dosage for medication with ID: {}", medicationId);

        try {
            PatientMedication medication = patientMedicationRepository.findById(medicationId)
                    .orElseThrow(() -> new PatientServiceException("Medication not found with id: " + medicationId));

            medication.setDosage(newDosage);
            PatientMedication updatedMedication = patientMedicationRepository.save(medication);

            return patientMedicationMapper.patientMedicationToPatientMedicationDto(updatedMedication);
        } catch (Exception e) {
            log.error("Error changing dosage: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to change dosage", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicationDto> getExpiringMedications(UUID patientId, int daysAhead) {
        log.info("Getting expiring medications for patient ID: {} within {} days", patientId, daysAhead);

        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        Date endDate = Date.from(futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<PatientMedication> currentMedications = patientMedicationRepository
                .findCurrentMedicationsByPatientId(patientId);

        return currentMedications.stream()
                .filter(med -> med.getEndDate() != null && med.getEndDate().before(endDate))
                .map(patientMedicationMapper::patientMedicationToPatientMedicationDto)
                .collect(Collectors.toList());
    }
}
