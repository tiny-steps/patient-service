package com.tintsteps.patientservice.service.impl;

import com.tintsteps.patientservice.dto.PatientDto;
import com.tintsteps.patientservice.dto.PatientRegistrationDto;
import com.tintsteps.patientservice.dto.UserRegistrationRequest;
import com.tintsteps.patientservice.exception.PatientNotFoundException;
import com.tintsteps.patientservice.exception.PatientServiceException;
import com.tintsteps.patientservice.integration.AuthServiceIntegration;
import com.tintsteps.patientservice.integration.UserServiceIntegration;
import com.tintsteps.patientservice.integration.dto.UserDto;
import com.tintsteps.patientservice.integration.model.UserModel;
import com.tintsteps.patientservice.integration.model.UserUpdateRequest;
import com.tintsteps.patientservice.mapper.PatientMapper;
import com.tintsteps.patientservice.model.Gender;
import com.tintsteps.patientservice.model.Patient;
import com.tintsteps.patientservice.repository.PatientRepository;
import com.tintsteps.patientservice.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final UserServiceIntegration userServiceIntegration;

    @Override
    @Transactional
    @CacheEvict(value = "patients", allEntries = true)
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
    @Cacheable(value = "patients", key = "#id")
    public PatientDto findById(UUID id) {
        log.info("Finding patient by ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));

        return patientMapper.patientToPatientDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patients", key = "'user-' + #userId")
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
    @CacheEvict(value = "patients", key = "#id")
    public PatientDto update(UUID id, PatientDto patientDto) {
        log.info("Updating patient with ID: {}", id);

        try {
            Patient existingPatient = patientRepository.findById(id)
                    .orElseThrow(() -> new PatientNotFoundException(id));

            // Store original values for comparison
            UUID originalUserId = existingPatient.getUserId();

            // Get current user information for comparison
            String originalName = null;
            String originalEmail = null;
            String originalPhone = null;
            String originalAvatar = null;
            String originalStatus = null;

            if (existingPatient.getUserId() != null) {
                try {
                    var currentUser = userServiceIntegration.getUserByIdReactive(existingPatient.getUserId()).block();
                    if (currentUser != null) {
                        originalName = currentUser.getFirstName() + " " + currentUser.getLastName();
                        originalEmail = currentUser.getEmail();
                        originalPhone = currentUser.getPhoneNumber();
                        originalAvatar = null; // UserDto doesn't have avatar field
                        originalStatus = currentUser.isActive() ? "ACTIVE" : "INACTIVE";
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch current user information for comparison: {}", e.getMessage());
                }
            }

            // Update patient entity fields
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

            // Update user information if patient has a userId
            if (existingPatient.getUserId() != null) {
                try {
                    log.info("Updating user information for patient ID: {} with userId: {}", id,
                            existingPatient.getUserId());

                    // Update user in user service with current values
                    UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                            .name(originalName) // Keep current name since patient entity doesn't store it
                            .email(originalEmail) // Keep current email
                            .phone(originalPhone) // Keep current phone
                            .avatar(originalAvatar) // Keep current avatar
                            .status(originalStatus) // Keep current status
                            .build();

                    userServiceIntegration.updateUser(existingPatient.getUserId(), userUpdateRequest)
                            .doOnSuccess(
                                    user -> log.info("Successfully updated user information for patient ID: {}", id))
                            .doOnError(error -> log.error("Failed to update user information for patient ID: {}", id,
                                    error))
                            .subscribe();
                } catch (Exception e) {
                    log.error("Error updating user information for patient ID: {}", id, e);
                    // Don't fail the patient update if user update fails
                }
            }

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
    @CacheEvict(value = "patients", key = "#id")
    public PatientDto partialUpdate(UUID id, PatientDto patientDto) {
        return update(id, patientDto); // Same implementation for now
    }

    @Override
    @Transactional
    @CacheEvict(value = "patients", key = "#id")
    public void delete(UUID id) {
        log.info("Starting deletion process for patient with ID: {}", id);

        try {
            // Find the patient first to get the userId
            var patient = patientRepository.findById(id)
                    .orElseThrow(() -> new PatientNotFoundException(id));

            UUID userId = patient.getUserId();
            log.info("Found patient with ID: {} and userId: {}", id, userId);

            // Step 1: Delete the patient record first
            log.info("Step 1: Deleting patient record with ID: {}", id);
            patientRepository.deleteById(id);
            log.info("Successfully deleted patient record with ID: {}", id);

            // Step 2: Delete the user from both auth-service and user-service if userId
            // exists
            if (userId != null) {
                log.info("Step 2: Deleting user with ID: {} from auth-service and user-service", userId);

                try {
                    // Delete from auth-service
                    authServiceIntegration.deleteUser(userId.toString())
                            .doOnSuccess(
                                    result -> log.info("Successfully deleted user from auth-service with ID: {}",
                                            userId))
                            .doOnError(error -> log.error("Failed to delete user from auth-service with ID: {}", userId,
                                    error))
                            .subscribe();

                    // Delete from user-service
                    userServiceIntegration.deleteUser(userId)
                            .doOnSuccess(
                                    result -> log.info("Successfully deleted user from user-service with ID: {}",
                                            userId))
                            .doOnError(error -> log.error("Failed to delete user from user-service with ID: {}", userId,
                                    error))
                            .subscribe();

                    log.info("User deletion requests sent to both auth-service and user-service for user ID: {}",
                            userId);

                } catch (Exception e) {
                    log.error("Error during user deletion process for user ID: {}", userId, e);
                    // Don't fail the patient deletion if user deletion fails
                    // The user deletion is asynchronous and will be handled by the respective
                    // services
                }
            } else {
                log.info("No userId associated with patient ID: {}, skipping user deletion", id);
            }

            log.info("Completed deletion process for patient with ID: {}", id);
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
    @CacheEvict(value = "patients", key = "#id")
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
        BigDecimal heightInMeters = BigDecimal.valueOf(patient.getHeightCm()).divide(BigDecimal.valueOf(100), 2,
                RoundingMode.HALF_UP);
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

        if (patient.getDateOfBirth() == null)
            missingFields.add("dateOfBirth");
        if (patient.getGender() == null)
            missingFields.add("gender");
        if (patient.getBloodGroup() == null)
            missingFields.add("bloodGroup");
        if (patient.getHeightCm() == null)
            missingFields.add("height");
        if (patient.getWeightKg() == null)
            missingFields.add("weight");

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

        if (patient.getDateOfBirth() != null)
            completedFields++;
        if (patient.getGender() != null)
            completedFields++;
        if (patient.getBloodGroup() != null)
            completedFields++;
        if (patient.getHeightCm() != null)
            completedFields++;
        if (patient.getWeightKg() != null)
            completedFields++;

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
        // This would typically be implemented by checking if the patient has emergency
        // contacts
        // For now, return false as a placeholder - this should be implemented with
        // proper integration
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
    @CacheEvict(value = "patients", key = "#id")
    public PatientDto updateEmail(UUID id, String newEmail) {
        log.info("Updating email for patient ID: {} to: {}", id, newEmail);

        try {
            Patient existingPatient = patientRepository.findById(id)
                    .orElseThrow(() -> new PatientNotFoundException(id));

            if (existingPatient.getUserId() == null) {
                throw new PatientServiceException("Patient does not have an associated user ID");
            }

            // Update email in auth service
            authServiceIntegration.updateUserEmail(existingPatient.getUserId().toString(), newEmail)
                    .doOnSuccess(
                            result -> log.info("Successfully updated email in auth service for patient ID: {}", id))
                    .doOnError(
                            error -> log.error("Failed to update email in auth service for patient ID: {}", id, error))
                    .subscribe();

            // Update email in user service
            UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                    .name(null) // Keep current name
                    .email(newEmail)
                    .phone(null) // Keep current phone
                    .avatar(null) // Keep current avatar
                    .status(null) // Keep current status
                    .build();

            userServiceIntegration.updateUser(existingPatient.getUserId(), userUpdateRequest)
                    .doOnSuccess(
                            result -> log.info("Successfully updated email in user service for patient ID: {}", id))
                    .doOnError(
                            error -> log.error("Failed to update email in user service for patient ID: {}", id, error))
                    .subscribe();

            log.info("Email update requests sent to both auth-service and user-service for patient ID: {}", id);
            return patientMapper.patientToPatientDto(existingPatient);

        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating email for patient ID: {}", id, e);
            throw new PatientServiceException("Failed to update email", e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "patients", allEntries = true)
    public PatientDto registerPatient(PatientRegistrationDto registrationDto) {
        log.info("Registering new patient with email: {}", registrationDto.getEmail());

        try {
            // Step 1: Register user via auth-service using integration
            UserRegistrationRequest userRequest = UserRegistrationRequest.builder()
                    .name(registrationDto.getName())
                    .email(registrationDto.getEmail())
                    .password(registrationDto.getPassword())
                    .phone(registrationDto.getPhone())
                    .role(registrationDto.getRole())
                    .build();

            UserModel userResponse = authServiceIntegration.registerUser(userRequest).block();

            if (userResponse == null || userResponse.id() == null) {
                throw new PatientServiceException("Failed to register user - no user ID returned");
            }

            UUID userId = UUID.fromString(userResponse.id());
            log.info("User registered successfully with ID: {}", userId);

            // Step 2: Create patient with the returned user ID
            PatientDto patientDto = new PatientDto();
            patientDto.setUserId(userId);
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
