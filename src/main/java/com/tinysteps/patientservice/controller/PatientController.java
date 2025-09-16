package com.tinysteps.patientservice.controller;

import com.tinysteps.patientservice.dto.PatientDto;
import com.tinysteps.patientservice.dto.PatientRegistrationDto;
import com.tinysteps.patientservice.model.Gender;
import com.tinysteps.patientservice.model.ResponseModel;
import com.tinysteps.patientservice.service.PatientService;
import com.tinysteps.patientservice.service.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT')")
    @CacheEvict(value = "patients", allEntries = true)
    public ResponseEntity<ResponseModel<PatientDto>> createPatient(
            @Valid @RequestBody PatientDto patientDto,
            Authentication authentication) {
        PatientDto createdPatient = patientService.create(patientDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdPatient, "Patient created successfully"));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PATIENT')")
    @CacheEvict(value = "patients", allEntries = true)
    public ResponseEntity<ResponseModel<PatientDto>> registerPatient(
            @Valid @RequestBody PatientRegistrationDto registrationDto,
            Authentication authentication) {
        PatientDto createdPatient = patientService.registerPatient(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdPatient, "Patient registered and created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #id)")
    @Cacheable(value = "patients", key = "#id")
    public ResponseEntity<ResponseModel<PatientDto>> getPatientById(@PathVariable UUID id) {
        PatientDto patient = patientService.findById(id);
        return ResponseEntity.ok(ResponseModel.success(patient, "Patient retrieved successfully"));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #userId)")
    @Cacheable(value = "patients", key = "'user-' + #userId")
    public ResponseEntity<ResponseModel<PatientDto>> getPatientByUserId(@PathVariable UUID userId) {
        PatientDto patient = patientService.findByUserId(userId);
        return ResponseEntity.ok(ResponseModel.success(patient, "Patient retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> getAllPatients(Pageable pageable) {
        Page<PatientDto> patients = patientService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.success(patients, "Patients retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #id)")
    @CacheEvict(value = "patients", key = "#id")
    public ResponseEntity<ResponseModel<PatientDto>> updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody PatientDto patientDto) {
        PatientDto updatedPatient = patientService.update(id, patientDto);
        return ResponseEntity.ok(ResponseModel.success(updatedPatient, "Patient updated successfully"));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #id)")
    @CacheEvict(value = "patients", key = "#id")
    public ResponseEntity<ResponseModel<PatientDto>> partialUpdatePatient(
            @PathVariable UUID id,
            @RequestBody PatientDto patientDto) {
        PatientDto updatedPatient = patientService.partialUpdate(id, patientDto);
        return ResponseEntity.ok(ResponseModel.success(updatedPatient, "Patient updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "patients", key = "#id")
    public ResponseEntity<ResponseModel<Void>> deletePatient(@PathVariable UUID id) {
        patientService.delete(id);
        return ResponseEntity.ok(ResponseModel.success(null, "Patient deleted successfully"));
    }

    // Soft delete endpoints
    @PatchMapping("/{id}/soft-delete")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "patients", key = "#id")
    public ResponseEntity<ResponseModel<PatientDto>> softDeletePatient(@PathVariable UUID id) {
        PatientDto deletedPatient = patientService.softDelete(id);
        return ResponseEntity.ok(ResponseModel.success(deletedPatient, "Patient soft deleted successfully"));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "patients", key = "#id")
    public ResponseEntity<ResponseModel<PatientDto>> reactivatePatient(@PathVariable UUID id) {
        PatientDto reactivatedPatient = patientService.reactivate(id);
        return ResponseEntity.ok(ResponseModel.success(reactivatedPatient, "Patient reactivated successfully"));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> getActivePatients(Pageable pageable) {
        Page<PatientDto> activePatients = patientService.findActivePatients(pageable);
        return ResponseEntity.ok(ResponseModel.success(activePatients, "Active patients retrieved successfully"));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> getDeletedPatients(Pageable pageable) {
        Page<PatientDto> deletedPatients = patientService.findDeletedPatients(pageable);
        return ResponseEntity.ok(ResponseModel.success(deletedPatients, "Deleted patients retrieved successfully"));
    }

    @GetMapping("/search/gender/{gender}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> getPatientsByGender(
            @PathVariable Gender gender,
            Pageable pageable) {
        Page<PatientDto> patients = patientService.findByGender(gender, pageable);
        return ResponseEntity.ok(ResponseModel.success(patients, "Patients retrieved successfully"));
    }

    @GetMapping("/search/blood-group/{bloodGroup}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> getPatientsByBloodGroup(
            @PathVariable String bloodGroup,
            Pageable pageable) {
        Page<PatientDto> patients = patientService.findByBloodGroup(bloodGroup, pageable);
        return ResponseEntity.ok(ResponseModel.success(patients, "Patients retrieved successfully"));
    }

    @GetMapping("/search/age")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> getPatientsByAgeRange(
            @RequestParam Integer minAge,
            @RequestParam Integer maxAge,
            Pageable pageable) {
        Page<PatientDto> patients = patientService.findByAgeBetween(minAge, maxAge, pageable);
        return ResponseEntity.ok(ResponseModel.success(patients, "Patients retrieved successfully"));
    }

    @PatchMapping("/{id}/medical-info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #id)")
    @CacheEvict(value = "patients", key = "#id")
    public ResponseEntity<ResponseModel<PatientDto>> updateMedicalInfo(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer heightCm,
            @RequestParam(required = false) BigDecimal weightKg,
            @RequestParam(required = false) String bloodGroup) {
        PatientDto updatedPatient = patientService.updateMedicalInfo(id, heightCm, weightKg, bloodGroup);
        return ResponseEntity.ok(ResponseModel.success(updatedPatient, "Medical information updated successfully"));
    }

    @PatchMapping("/{id}/personal-info")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #id)")
    @CacheEvict(value = "patients", key = "#id")
    public ResponseEntity<ResponseModel<PatientDto>> updatePersonalInfo(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateOfBirth,
            @RequestParam(required = false) Gender gender) {
        PatientDto updatedPatient = patientService.updatePersonalInfo(id, dateOfBirth, gender);
        return ResponseEntity.ok(ResponseModel.success(updatedPatient, "Personal information updated successfully"));
    }

    @PatchMapping("/{id}/email")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #id)")
    @CacheEvict(value = "patients", key = "#id")
    public ResponseEntity<ResponseModel<PatientDto>> updateEmail(
            @PathVariable UUID id,
            @RequestParam String newEmail) {
        PatientDto updatedPatient = patientService.updateEmail(id, newEmail);
        return ResponseEntity
                .ok(ResponseModel.success(updatedPatient, "Email updated successfully in both auth and user services"));
    }

    @GetMapping("/{id}/age")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<Integer>> calculateAge(@PathVariable UUID id) {
        int age = patientService.calculateAge(id);
        return ResponseEntity.ok(ResponseModel.success(age, "Age calculated successfully"));
    }

    @GetMapping("/{id}/bmi")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<BigDecimal>> calculateBMI(@PathVariable UUID id) {
        BigDecimal bmi = patientService.calculateBMI(id);
        return ResponseEntity.ok(ResponseModel.success(bmi, "BMI calculated successfully"));
    }

    @GetMapping("/{id}/profile-completeness")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<Integer>> getProfileCompleteness(@PathVariable UUID id) {
        int completeness = patientService.calculateProfileCompleteness(id);
        return ResponseEntity.ok(ResponseModel.success(completeness, "Profile completeness calculated successfully"));
    }

    @GetMapping("/{id}/missing-fields")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<List<String>>> getMissingFields(@PathVariable UUID id) {
        List<String> missingFields = patientService.getMissingProfileFields(id);
        return ResponseEntity.ok(ResponseModel.success(missingFields, "Missing fields retrieved successfully"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Object>> getStatistics() {
        return ResponseEntity.ok(ResponseModel.success(
                new Object() {
                    public final long totalPatients = patientService.countAll();
                    public final Double averageAge = patientService.getAverageAge();
                    public final Double averageHeight = patientService.getAverageHeight();
                    public final BigDecimal averageWeight = patientService.getAverageWeight();
                    public final List<Object[]> genderStats = patientService.getGenderStatistics();
                    public final List<Object[]> bloodGroupStats = patientService.getBloodGroupStatistics();
                    public final List<String> distinctBloodGroups = patientService.getDistinctBloodGroups();
                },
                "Patient statistics retrieved successfully"));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "patients", allEntries = true)
    public ResponseEntity<ResponseModel<List<PatientDto>>> createBatch(
            @Valid @RequestBody List<PatientDto> patientDtos) {
        List<PatientDto> createdPatients = patientService.createBatch(patientDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdPatients, "Batch patients created successfully"));
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "patients", allEntries = true)
    public ResponseEntity<ResponseModel<Void>> deleteBatch(@RequestBody List<UUID> ids) {
        patientService.deleteBatch(ids);
        return ResponseEntity.ok(ResponseModel.success("Batch patients deleted successfully"));
    }

    // Branch-specific endpoints
    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> getPatientsByBranch(
            @PathVariable UUID branchId,
            Pageable pageable) {
        // Validate branch access
        securityService.validateBranchAccess(branchId);
        
        Page<PatientDto> patients = patientService.findByBranchId(branchId, pageable);
        return ResponseEntity.ok(ResponseModel.success(patients, "Patients retrieved for branch successfully"));
    }

    @GetMapping("/my-branch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> getPatientsForMyBranch(Pageable pageable) {
        UUID primaryBranchId = securityService.getPrimaryBranchId();
        Page<PatientDto> patients = patientService.findByBranchId(primaryBranchId, pageable);
        return ResponseEntity.ok(ResponseModel.success(patients, "Patients retrieved for your branch successfully"));
    }
}
