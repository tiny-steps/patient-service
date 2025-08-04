package com.tintsteps.patientservice.controller;

import com.tintsteps.patientservice.dto.PatientAllergyDto;
import com.tintsteps.patientservice.model.ResponseModel;
import com.tintsteps.patientservice.service.PatientAllergyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/patient-allergies")
@RequiredArgsConstructor
public class PatientAllergyController {

    private final PatientAllergyService patientAllergyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientAllergyDto.patientId)")
    public ResponseEntity<ResponseModel<PatientAllergyDto>> createPatientAllergy(
            @Valid @RequestBody PatientAllergyDto patientAllergyDto) {
        PatientAllergyDto createdAllergy = patientAllergyService.create(patientAllergyDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdAllergy, "Patient allergy created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<PatientAllergyDto>> getPatientAllergyById(@PathVariable UUID id) {
        PatientAllergyDto allergy = patientAllergyService.findById(id);
        return ResponseEntity.ok(ResponseModel.success(allergy, "Patient allergy retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientAllergyDto>>> getAllPatientAllergies(Pageable pageable) {
        Page<PatientAllergyDto> allergies = patientAllergyService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.success(allergies, "Patient allergies retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientAllergyDto>>> getPatientAllergiesByPatientId(@PathVariable UUID patientId) {
        List<PatientAllergyDto> allergies = patientAllergyService.findByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success(allergies, "Patient allergies retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<PatientAllergyDto>> updatePatientAllergy(
            @PathVariable UUID id, @Valid @RequestBody PatientAllergyDto patientAllergyDto) {
        PatientAllergyDto updatedAllergy = patientAllergyService.update(id, patientAllergyDto);
        return ResponseEntity.ok(ResponseModel.success(updatedAllergy, "Patient allergy updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Void>> deletePatientAllergy(@PathVariable UUID id) {
        patientAllergyService.delete(id);
        return ResponseEntity.ok(ResponseModel.success("Patient allergy deleted successfully"));
    }

    @GetMapping("/allergen/{allergen}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientAllergyDto>>> getAllergiesByAllergen(
            @PathVariable String allergen, Pageable pageable) {
        Page<PatientAllergyDto> allergies = patientAllergyService.findByAllergen(allergen, pageable);
        return ResponseEntity.ok(ResponseModel.success(allergies, "Allergies retrieved successfully"));
    }

    @PostMapping("/patient/{patientId}/add")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientAllergyDto>> addAllergyToPatient(
            @PathVariable UUID patientId,
            @RequestParam String allergen,
            @RequestParam(required = false) String reaction) {
        PatientAllergyDto allergy = patientAllergyService.addAllergy(patientId, allergen, reaction);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(allergy, "Allergy added successfully"));
    }

    @DeleteMapping("/patient/{patientId}/remove")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> removeAllergyFromPatient(
            @PathVariable UUID patientId,
            @RequestParam String allergen) {
        patientAllergyService.removeAllergy(patientId, allergen);
        return ResponseEntity.ok(ResponseModel.success("Allergy removed successfully"));
    }

    @GetMapping("/patient/{patientId}/has-allergy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Boolean>> hasAllergy(
            @PathVariable UUID patientId,
            @RequestParam String allergen) {
        boolean hasAllergy = patientAllergyService.hasAllergy(patientId, allergen);
        return ResponseEntity.ok(ResponseModel.success(hasAllergy, "Allergy check completed"));
    }

    @GetMapping("/patient/{patientId}/critical")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<String>>> getCriticalAllergies(@PathVariable UUID patientId) {
        List<String> criticalAllergies = patientAllergyService.getCriticalAllergies(patientId);
        return ResponseEntity.ok(ResponseModel.success(criticalAllergies, "Critical allergies retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Integer>> getAllergyCount(@PathVariable UUID patientId) {
        int count = patientAllergyService.getAllergyCount(patientId);
        return ResponseEntity.ok(ResponseModel.success(count, "Allergy count retrieved successfully"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Object>> getAllergyStatistics() {
        return ResponseEntity.ok(ResponseModel.success(
                new Object() {
                    public final long totalAllergies = patientAllergyService.countAll();
                    public final List<Object[]> allergenStats = patientAllergyService.getAllergenStatistics();
                    public final List<String> distinctAllergens = patientAllergyService.getDistinctAllergens();
                    public final List<String> distinctReactions = patientAllergyService.getDistinctReactions();
                },
                "Allergy statistics retrieved successfully"
        ));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientAllergyDto>>> createBatch(
            @Valid @RequestBody List<PatientAllergyDto> patientAllergyDtos) {
        List<PatientAllergyDto> createdAllergies = patientAllergyService.createBatch(patientAllergyDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdAllergies, "Batch allergies created successfully"));
    }

    @DeleteMapping("/patient/{patientId}/all")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> deleteAllAllergiesForPatient(@PathVariable UUID patientId) {
        patientAllergyService.deleteByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success("All allergies deleted for patient"));
    }
}
