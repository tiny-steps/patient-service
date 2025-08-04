package com.tintsteps.patientservice.service.impl;

import com.tintsteps.patientservice.dto.PatientDto;
import com.tintsteps.patientservice.exception.PatientNotFoundException;
import com.tintsteps.patientservice.exception.PatientServiceException;
import com.tintsteps.patientservice.mapper.PatientMapper;
import com.tintsteps.patientservice.model.Gender;
import com.tintsteps.patientservice.model.Patient;
import com.tintsteps.patientservice.repository.PatientRepository;
import com.tintsteps.patientservice.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper = PatientMapper.INSTANCE;

    @Override
    @Transactional
    public PatientDto create(PatientDto patientDto) {
        log.info("Creating patient for user ID: {}", patientDto.getUserId());
        
        try {
            if (patientDto.getUserId() == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            
            if (existsByUserId(patientDto.getUserId())) {
                throw new PatientServiceException("Patient already exists for user ID: " + patientDto.getUserId());
            }
            
            Patient patient = patientMapper.patientDtoToPatient(patientDto);
            Patient savedPatient = patientRepository.save(patient);
            
            log.info("Patient created successfully with ID: {}", savedPatient.getId());
            return patientMapper.patientToPatientDto(savedPatient);
        } catch (Exception e) {
            log.error("Error creating patient: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create patient", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDto findById(UUID id) {
        log.info("Finding patient by ID: {}", id);
        
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
        
        return patientMapper.patientToPatientDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDto findByUserId(UUID userId) {
        log.info("Finding patient by user ID: {}", userId);
        
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException("userId", userId));
        
        return patientMapper.patientToPatientDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findAll(Pageable pageable) {
        log.info("Finding all patients with pagination: {}", pageable);
        
        Page<Patient> patients = patientRepository.findAll(pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional
    public PatientDto update(UUID id, PatientDto patientDto) {
        log.info("Updating patient with ID: {}", id);
        
        try {
            Patient existingPatient = patientRepository.findById(id)
                    .orElseThrow(() -> new PatientNotFoundException(id));
            
            // Update fields
            if (patientDto.getDateOfBirth() != null) {
                existingPatient.setDateOfBirth(patientDto.getDateOfBirth());
            }
            if (patientDto.getGender() != null) {
                existingPatient.setGender(patientDto.getGender());
            }
            if (patientDto.getBloodGroup() != null) {
                existingPatient.setBloodGroup(patientDto.getBloodGroup());
            }
            if (patientDto.getHeightCm() != null) {
                existingPatient.setHeightCm(patientDto.getHeightCm());
            }
            if (patientDto.getWeightKg() != null) {
                existingPatient.setWeightKg(patientDto.getWeightKg());
            }
            
            Patient updatedPatient = patientRepository.save(existingPatient);
            
            log.info("Patient updated successfully with ID: {}", updatedPatient.getId());
            return patientMapper.patientToPatientDto(updatedPatient);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating patient: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to update patient", e);
        }
    }

    @Override
    @Transactional
    public PatientDto partialUpdate(UUID id, PatientDto patientDto) {
        return update(id, patientDto); // Same implementation for now
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting patient with ID: {}", id);
        
        try {
            if (!existsById(id)) {
                throw new PatientNotFoundException(id);
            }
            
            patientRepository.deleteById(id);
            log.info("Patient deleted successfully with ID: {}", id);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting patient: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete patient", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByGender(Gender gender) {
        log.info("Finding patients by gender: {}", gender);
        
        List<Patient> patients = patientRepository.findByGender(gender);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByGender(Gender gender, Pageable pageable) {
        log.info("Finding patients by gender: {} with pagination", gender);
        
        Page<Patient> patients = patientRepository.findByGender(gender, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByBloodGroup(String bloodGroup) {
        log.info("Finding patients by blood group: {}", bloodGroup);
        
        List<Patient> patients = patientRepository.findByBloodGroup(bloodGroup);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByBloodGroup(String bloodGroup, Pageable pageable) {
        log.info("Finding patients by blood group: {} with pagination", bloodGroup);
        
        Page<Patient> patients = patientRepository.findByBloodGroup(bloodGroup, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByAgeBetween(Integer minAge, Integer maxAge) {
        log.info("Finding patients by age range: {} - {}", minAge, maxAge);
        
        List<Patient> patients = patientRepository.findByAgeBetween(minAge, maxAge);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByAgeBetween(Integer minAge, Integer maxAge, Pageable pageable) {
        log.info("Finding patients by age range: {} - {} with pagination", minAge, maxAge);
        
        Page<Patient> patients = patientRepository.findByAgeBetween(minAge, maxAge, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByHeightRange(Integer minHeight, Integer maxHeight) {
        log.info("Finding patients by height range: {} - {}", minHeight, maxHeight);
        
        List<Patient> patients = patientRepository.findByHeightCmBetween(minHeight, maxHeight);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByHeightRange(Integer minHeight, Integer maxHeight, Pageable pageable) {
        log.info("Finding patients by height range: {} - {} with pagination", minHeight, maxHeight);
        
        Page<Patient> patients = patientRepository.findByHeightCmBetween(minHeight, maxHeight, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByWeightRange(BigDecimal minWeight, BigDecimal maxWeight) {
        log.info("Finding patients by weight range: {} - {}", minWeight, maxWeight);
        
        List<Patient> patients = patientRepository.findByWeightKgBetween(minWeight, maxWeight);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByWeightRange(BigDecimal minWeight, BigDecimal maxWeight, Pageable pageable) {
        log.info("Finding patients by weight range: {} - {} with pagination", minWeight, maxWeight);
        
        Page<Patient> patients = patientRepository.findByWeightKgBetween(minWeight, maxWeight, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByDateOfBirthRange(Date startDate, Date endDate) {
        log.info("Finding patients by date of birth range: {} - {}", startDate, endDate);
        
        List<Patient> patients = patientRepository.findByDateOfBirthBetween(startDate, endDate);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByDateOfBirthRange(Date startDate, Date endDate, Pageable pageable) {
        log.info("Finding patients by date of birth range: {} - {} with pagination", startDate, endDate);

        Page<Patient> patients = patientRepository.findByDateOfBirthBetween(startDate, endDate, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> searchPatients(Gender gender, String bloodGroup, Integer minAge, Integer maxAge,
                                          Integer minHeight, Integer maxHeight, BigDecimal minWeight,
                                          BigDecimal maxWeight, Pageable pageable) {
        log.info("Searching patients with multiple criteria");

        // For now, implement basic search - can be enhanced with Specifications
        Page<Patient> patients = patientRepository.findAll(pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional
    public PatientDto updateMedicalInfo(UUID id, Integer heightCm, BigDecimal weightKg, String bloodGroup) {
        log.info("Updating medical info for patient ID: {}", id);

        try {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new PatientNotFoundException(id));

            if (heightCm != null) {
                patient.setHeightCm(heightCm);
            }
            if (weightKg != null) {
                patient.setWeightKg(weightKg);
            }
            if (bloodGroup != null) {
                patient.setBloodGroup(bloodGroup);
            }

            Patient updatedPatient = patientRepository.save(patient);
            return patientMapper.patientToPatientDto(updatedPatient);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating medical info: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to update medical info", e);
        }
    }

    @Override
    @Transactional
    public PatientDto updatePersonalInfo(UUID id, Date dateOfBirth, Gender gender) {
        log.info("Updating personal info for patient ID: {}", id);

        try {
            Patient patient = patientRepository.findById(id)
                    .orElseThrow(() -> new PatientNotFoundException(id));

            if (dateOfBirth != null) {
                patient.setDateOfBirth(dateOfBirth);
            }
            if (gender != null) {
                patient.setGender(gender);
            }

            Patient updatedPatient = patientRepository.save(patient);
            return patientMapper.patientToPatientDto(updatedPatient);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating personal info: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to update personal info", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int calculateAge(UUID id) {
        log.info("Calculating age for patient ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        if (patient.getDateOfBirth() == null) {
            throw new PatientServiceException("Date of birth not available for patient");
        }

        LocalDate birthDate = patient.getDateOfBirth().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = LocalDate.now();

        return Period.between(birthDate, currentDate).getYears();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateBMI(UUID id) {
        log.info("Calculating BMI for patient ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        if (patient.getHeightCm() == null || patient.getWeightKg() == null) {
            throw new PatientServiceException("Height and weight required for BMI calculation");
        }

        // BMI = weight(kg) / (height(m))^2
        BigDecimal heightInMeters = BigDecimal.valueOf(patient.getHeightCm()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal heightSquared = heightInMeters.multiply(heightInMeters);

        return patient.getWeightKg().divide(heightSquared, 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return patientRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return patientRepository.existsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPatientProfileComplete(UUID id) {
        return calculateProfileCompleteness(id) >= 80; // 80% threshold
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getMissingProfileFields(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        List<String> missingFields = new ArrayList<>();

        if (patient.getDateOfBirth() == null) missingFields.add("dateOfBirth");
        if (patient.getGender() == null) missingFields.add("gender");
        if (patient.getBloodGroup() == null) missingFields.add("bloodGroup");
        if (patient.getHeightCm() == null) missingFields.add("height");
        if (patient.getWeightKg() == null) missingFields.add("weight");

        return missingFields;
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return patientRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByGender(Gender gender) {
        return patientRepository.countByGender(gender);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByBloodGroup(String bloodGroup) {
        return patientRepository.countByBloodGroup(bloodGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAgeRange(Integer minAge, Integer maxAge) {
        return patientRepository.countByAgeBetween(minAge, maxAge);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageAge() {
        return patientRepository.getAverageAge();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageHeight() {
        return patientRepository.getAverageHeight();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageWeight() {
        return patientRepository.getAverageWeight();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getGenderStatistics() {
        return patientRepository.getGenderStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getBloodGroupStatistics() {
        return patientRepository.getBloodGroupStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctBloodGroups() {
        return patientRepository.findDistinctBloodGroups();
    }

    @Override
    @Transactional
    public List<PatientDto> createBatch(List<PatientDto> patientDtos) {
        log.info("Creating batch of {} patients", patientDtos.size());

        try {
            List<Patient> patients = patientDtos.stream()
                    .map(patientMapper::patientDtoToPatient)
                    .collect(Collectors.toList());

            List<Patient> savedPatients = patientRepository.saveAll(patients);

            return savedPatients.stream()
                    .map(patientMapper::patientToPatientDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error creating batch of patients: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create batch of patients", e);
        }
    }

    @Override
    @Transactional
    public void deleteBatch(List<UUID> ids) {
        log.info("Deleting batch of {} patients", ids.size());

        try {
            patientRepository.deleteAllById(ids);
        } catch (Exception e) {
            log.error("Error deleting batch of patients: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete batch of patients", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int calculateProfileCompleteness(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        int totalFields = 5; // userId, dateOfBirth, gender, bloodGroup, height, weight
        int completedFields = 1; // userId is always present

        if (patient.getDateOfBirth() != null) completedFields++;
        if (patient.getGender() != null) completedFields++;
        if (patient.getBloodGroup() != null) completedFields++;
        if (patient.getHeightCm() != null) completedFields++;
        if (patient.getWeightKg() != null) completedFields++;

        return (completedFields * 100) / totalFields;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasCompleteBasicInfo(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        return patient.getDateOfBirth() != null && patient.getGender() != null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMedicalInfo(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        return patient.getBloodGroup() != null || patient.getHeightCm() != null || patient.getWeightKg() != null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByAgeRange(Integer minAge, Integer maxAge, Pageable pageable) {
        Page<Patient> patients = patientRepository.findByAgeRange(minAge, maxAge, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByGender(Gender gender, Pageable pageable) {
        Page<Patient> patients = patientRepository.findByGender(gender, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByBloodGroup(String bloodGroup, Pageable pageable) {
        Page<Patient> patients = patientRepository.findByBloodGroup(bloodGroup, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEmergencyContacts(UUID patientId) {
        // This would typically be implemented by checking if the patient has emergency contacts
        // For now, return false as a placeholder - this should be implemented with proper integration
        return false;
    }

    // Missing search method implementations
    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByGender(Gender gender) {
        List<Patient> patients = patientRepository.findByGender(gender);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByBloodGroup(String bloodGroup) {
        List<Patient> patients = patientRepository.findByBloodGroup(bloodGroup);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByAgeBetween(Integer minAge, Integer maxAge) {
        List<Patient> patients = patientRepository.findByAgeBetween(minAge, maxAge);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByAgeBetween(Integer minAge, Integer maxAge, Pageable pageable) {
        Page<Patient> patients = patientRepository.findByAgeBetween(minAge, maxAge, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByHeightRange(Integer minHeight, Integer maxHeight) {
        List<Patient> patients = patientRepository.findByHeightRange(minHeight, maxHeight);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByHeightRange(Integer minHeight, Integer maxHeight, Pageable pageable) {
        Page<Patient> patients = patientRepository.findByHeightRange(minHeight, maxHeight, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByWeightRange(BigDecimal minWeight, BigDecimal maxWeight) {
        List<Patient> patients = patientRepository.findByWeightRange(minWeight, maxWeight);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByWeightRange(BigDecimal minWeight, BigDecimal maxWeight, Pageable pageable) {
        Page<Patient> patients = patientRepository.findByWeightRange(minWeight, maxWeight, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findByDateOfBirthRange(Date startDate, Date endDate) {
        List<Patient> patients = patientRepository.findByDateOfBirthRange(startDate, endDate);
        return patients.stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByDateOfBirthRange(Date startDate, Date endDate, Pageable pageable) {
        Page<Patient> patients = patientRepository.findByDateOfBirthRange(startDate, endDate, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }
}
