package com.tintsteps.patientservice.controller;

import com.tintsteps.patientservice.dto.PatientInsuranceDto;
import com.tintsteps.patientservice.model.ResponseModel;
import com.tintsteps.patientservice.service.PatientInsuranceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/patient-insurance")
@RequiredArgsConstructor
public class PatientInsuranceController {

    private final PatientInsuranceService patientInsuranceService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientInsuranceDto.patientId)")
    public ResponseEntity<ResponseModel<PatientInsuranceDto>> createInsurance(
            @Valid @RequestBody PatientInsuranceDto patientInsuranceDto) {
        PatientInsuranceDto createdInsurance = patientInsuranceService.create(patientInsuranceDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdInsurance, "Patient insurance created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientInsuranceOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientInsuranceDto>> getInsuranceById(@PathVariable UUID id) {
        PatientInsuranceDto insurance = patientInsuranceService.findById(id);
        return ResponseEntity.ok(ResponseModel.success(insurance, "Patient insurance retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientInsuranceDto>>> getAllInsurance(Pageable pageable) {
        Page<PatientInsuranceDto> insurance = patientInsuranceService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.success(insurance, "Patient insurance retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientInsuranceOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientInsuranceDto>> updateInsurance(
            @PathVariable UUID id, @Valid @RequestBody PatientInsuranceDto patientInsuranceDto) {
        PatientInsuranceDto updatedInsurance = patientInsuranceService.update(id, patientInsuranceDto);
        return ResponseEntity.ok(ResponseModel.success(updatedInsurance, "Patient insurance updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientInsuranceOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<Void>> deleteInsurance(@PathVariable UUID id) {
        patientInsuranceService.delete(id);
        return ResponseEntity.ok(ResponseModel.success("Patient insurance deleted successfully"));
    }

    // Search endpoints
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientInsuranceDto>>> getInsuranceByPatientId(@PathVariable UUID patientId) {
        List<PatientInsuranceDto> insurance = patientInsuranceService.findByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success(insurance, "Patient insurance retrieved successfully"));
    }

    @GetMapping("/provider/{provider}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientInsuranceDto>>> getInsuranceByProvider(
            @PathVariable String provider, Pageable pageable) {
        Page<PatientInsuranceDto> insurance = patientInsuranceService.findByProvider(provider, pageable);
        return ResponseEntity.ok(ResponseModel.success(insurance, "Insurance by provider retrieved successfully"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientInsuranceDto>>> searchInsurance(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String policyNumber,
            @RequestParam(required = false) String coverageDetails,
            Pageable pageable) {
        Page<PatientInsuranceDto> insurance = patientInsuranceService.searchInsurance(
                patientId, provider, policyNumber, coverageDetails, pageable);
        return ResponseEntity.ok(ResponseModel.success(insurance, "Insurance search completed successfully"));
    }

    // Business operations
    @PostMapping("/patient/{patientId}/add")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientInsuranceDto>> addInsurance(
            @PathVariable UUID patientId,
            @RequestParam String provider,
            @RequestParam String policyNumber,
            @RequestParam(required = false) String coverageDetails) {
        PatientInsuranceDto insurance = patientInsuranceService.addInsurance(patientId, provider, policyNumber, coverageDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(insurance, "Insurance added successfully"));
    }

    @DeleteMapping("/patient/{patientId}/remove")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> removeInsurance(
            @PathVariable UUID patientId,
            @RequestParam String policyNumber) {
        patientInsuranceService.removeInsurance(patientId, policyNumber);
        return ResponseEntity.ok(ResponseModel.success("Insurance removed successfully"));
    }

    @GetMapping("/patient/{patientId}/check/{policyNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Boolean>> hasInsurance(
            @PathVariable UUID patientId, @PathVariable String policyNumber) {
        boolean hasInsurance = patientInsuranceService.hasInsurance(patientId, policyNumber);
        return ResponseEntity.ok(ResponseModel.success(hasInsurance, "Insurance check completed"));
    }

    @GetMapping("/patient/{patientId}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Integer>> getInsuranceCount(@PathVariable UUID patientId) {
        int count = patientInsuranceService.getInsuranceCount(patientId);
        return ResponseEntity.ok(ResponseModel.success(count, "Insurance count retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/providers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<String>>> getInsuranceProviders(@PathVariable UUID patientId) {
        List<String> providers = patientInsuranceService.getInsuranceProviders(patientId);
        return ResponseEntity.ok(ResponseModel.success(providers, "Insurance providers retrieved successfully"));
    }

    // Statistics endpoints
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Object>> getInsuranceStatistics() {
        return ResponseEntity.ok(ResponseModel.success(
                new Object() {
                    public final long totalInsurance = patientInsuranceService.countAll();
                    public final List<Object[]> providerStats = patientInsuranceService.getProviderStatistics();
                    public final List<String> distinctProviders = patientInsuranceService.getDistinctProviders();
                    public final List<UUID> patientsWithMultipleInsurances = patientInsuranceService.findPatientsWithMultipleInsurances();
                    public final List<UUID> patientsWithoutInsurance = patientInsuranceService.findPatientsWithoutInsurance();
                },
                "Insurance statistics retrieved successfully"
        ));
    }

    // Bulk operations
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientInsuranceDto>>> createBatch(
            @Valid @RequestBody List<PatientInsuranceDto> patientInsuranceDtos) {
        List<PatientInsuranceDto> createdInsurance = patientInsuranceService.createBatch(patientInsuranceDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdInsurance, "Batch insurance created successfully"));
    }

    @DeleteMapping("/patient/{patientId}/all")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> deleteAllInsuranceForPatient(@PathVariable UUID patientId) {
        patientInsuranceService.deleteByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success("All insurance deleted for patient"));
    }
}
