package com.tintsteps.patientservice.service.impl;

import com.tintsteps.patientservice.dto.PatientDto;
import com.tintsteps.patientservice.dto.PatientRegistrationDto;
import com.tintsteps.patientservice.dto.UserRegistrationRequest;
import com.tintsteps.patientservice.dto.UserRegistrationResponse;
import com.tintsteps.patientservice.exception.PatientNotFoundException;
import com.tintsteps.patientservice.exception.PatientServiceException;
import com.tintsteps.patientservice.integration.AuthServiceIntegration;
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
    private final AuthServiceIntegration authServiceIntegration;

    @Override
    @Transactional
    public PatientDto create(PatientDto patientDto) {
        log.info("Creating patient for user ID: {}", patientDto.getId());

        try {
            if (patientDto.getId() == null) {
                throw new IllegalArgumentException("User ID is required");
            }

            if (existsByUserId(patientDto.getId())) {
                throw new PatientServiceException("Patient already exists for user ID: " + patientDto.getId());
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
    public Page<PatientDto> findByGender(Gender gender, Pageable pageable) {
        log.info("Finding patients by gender: {} with pagination", gender);

        Page<Patient> patients = patientRepository.findByGender(gender, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientDto> findByBloodGroup(String bloodGroup, Pageable pageable) {
        log.info("Finding patients by blood group: {} with pagination", bloodGroup);

        Page<Patient> patients = patientRepository.findByBloodGroup(bloodGroup, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }


    @Override
    public List<PatientDto> findAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::patientToPatientDto)
                .collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return patientRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByUserId(UUID userId) {
        return patientRepository.existsByUserId(userId);
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

    @Transactional(readOnly = true)
    @Override
    public boolean hasCompleteBasicInfo(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        return patient.getDateOfBirth() != null && patient.getGender() != null;
    }

    @Transactional(readOnly = true)
    @Override
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
    public Page<PatientDto> findByAgeBetween(Integer minAge, Integer maxAge, Pageable pageable) {
        Page<Patient> patients = patientRepository.findByAgeBetween(minAge, maxAge, pageable);
        return patients.map(patientMapper::patientToPatientDto);
    }

    @Override
    @Transactional
    public PatientDto registerPatient(PatientRegistrationDto registrationDto) {
        log.info("Registering new patient with email: {}", registrationDto.getEmail());

        try {
            // Step 1: Register user via auth-service using integration
            UserRegistrationRequest userRequest = UserRegistrationRequest.builder()
                    .name(registrationDto.getName())
                    .email(registrationDto.getEmail())
                    .password(registrationDto.getPassword())
                    .role(registrationDto.getRole())
                    .build();

            UserRegistrationResponse userResponse = authServiceIntegration.registerUser(userRequest).get();

            if (userResponse == null || userResponse.getId() == null) {
                throw new PatientServiceException("Failed to register user - no user ID returned");
            }

            UUID userId = userResponse.getId();
            log.info("User registered successfully with ID: {}", userId);

            // Step 2: Create patient with the returned user ID
            PatientDto patientDto = new PatientDto();
            patientDto.setId(userId);
            patientDto.setDateOfBirth(registrationDto.getDateOfBirth());
            patientDto.setGender(registrationDto.getGender());
            patientDto.setBloodGroup(registrationDto.getBloodGroup());
            patientDto.setHeightCm(registrationDto.getHeightCm());
            patientDto.setWeightKg(registrationDto.getWeightKg());

            Patient patient = patientMapper.patientDtoToPatient(patientDto);
            Patient savedPatient = patientRepository.save(patient);

            log.info("Patient registered and created successfully with ID: {}", savedPatient.getId());
            return patientMapper.patientToPatientDto(savedPatient);

        } catch (Exception e) {
            log.error("Error registering patient: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to register patient", e);
        }
    }
}
