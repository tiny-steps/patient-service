package com.tinysteps.patientservice.controller;

import com.tinysteps.patientservice.dto.PatientMedicalHistoryDto;
import com.tinysteps.patientservice.model.ResponseModel;
import com.tinysteps.patientservice.service.PatientMedicalHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/patient-medical-history")
@RequiredArgsConstructor
public class PatientMedicalHistoryController {

    private final PatientMedicalHistoryService patientMedicalHistoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientMedicalHistoryDto.patientId)")
    public ResponseEntity<ResponseModel<PatientMedicalHistoryDto>> createMedicalHistory(
            @Valid @RequestBody PatientMedicalHistoryDto patientMedicalHistoryDto) {
        PatientMedicalHistoryDto createdHistory = patientMedicalHistoryService.create(patientMedicalHistoryDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdHistory, "Medical history created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientMedicalHistoryOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientMedicalHistoryDto>> getMedicalHistoryById(@PathVariable UUID id) {
        PatientMedicalHistoryDto history = patientMedicalHistoryService.findById(id);
        return ResponseEntity.ok(ResponseModel.success(history, "Medical history retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientMedicalHistoryDto>>> getAllMedicalHistory(Pageable pageable) {
        Page<PatientMedicalHistoryDto> history = patientMedicalHistoryService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.success(history, "Medical history retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientMedicalHistoryOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientMedicalHistoryDto>> updateMedicalHistory(
            @PathVariable UUID id, @Valid @RequestBody PatientMedicalHistoryDto patientMedicalHistoryDto) {
        PatientMedicalHistoryDto updatedHistory = patientMedicalHistoryService.update(id, patientMedicalHistoryDto);
        return ResponseEntity.ok(ResponseModel.success(updatedHistory, "Medical history updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientMedicalHistoryOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<Void>> deleteMedicalHistory(@PathVariable UUID id) {
        patientMedicalHistoryService.delete(id);
        return ResponseEntity.ok(ResponseModel.success("Medical history deleted successfully"));
    }

    // Search endpoints
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientMedicalHistoryDto>>> getMedicalHistoryByPatientId(@PathVariable UUID patientId) {
        List<PatientMedicalHistoryDto> history = patientMedicalHistoryService.findByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success(history, "Medical history retrieved successfully"));
    }

    @GetMapping("/condition/{condition}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientMedicalHistoryDto>>> getMedicalHistoryByCondition(
            @PathVariable String condition, Pageable pageable) {
        Page<PatientMedicalHistoryDto> history = patientMedicalHistoryService.findByCondition(condition, pageable);
        return ResponseEntity.ok(ResponseModel.success(history, "Medical history by condition retrieved successfully"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientMedicalHistoryDto>>> searchMedicalHistory(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            Pageable pageable) {
        Page<PatientMedicalHistoryDto> history = patientMedicalHistoryService.searchMedicalHistory(
                patientId, condition, notes, startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseModel.success(history, "Medical history search completed successfully"));
    }

    // Business operations
    @PostMapping("/patient/{patientId}/add")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientMedicalHistoryDto>> addMedicalHistory(
            @PathVariable UUID patientId,
            @RequestParam String condition,
            @RequestParam(required = false) String notes) {
        PatientMedicalHistoryDto history = patientMedicalHistoryService.addMedicalHistory(patientId, condition, notes);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(history, "Medical history added successfully"));
    }

    @DeleteMapping("/patient/{patientId}/remove")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> removeMedicalHistory(
            @PathVariable UUID patientId,
            @RequestParam String condition) {
        patientMedicalHistoryService.removeMedicalHistory(patientId, condition);
        return ResponseEntity.ok(ResponseModel.success("Medical history removed successfully"));
    }

    @GetMapping("/patient/{patientId}/check/{condition}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Boolean>> hasMedicalHistory(
            @PathVariable UUID patientId, @PathVariable String condition) {
        boolean hasHistory = patientMedicalHistoryService.hasMedicalHistory(patientId, condition);
        return ResponseEntity.ok(ResponseModel.success(hasHistory, "Medical history check completed"));
    }

    @GetMapping("/patient/{patientId}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Integer>> getMedicalHistoryCount(@PathVariable UUID patientId) {
        int count = patientMedicalHistoryService.getMedicalHistoryCount(patientId);
        return ResponseEntity.ok(ResponseModel.success(count, "Medical history count retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/conditions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<String>>> getMedicalConditions(@PathVariable UUID patientId) {
        List<String> conditions = patientMedicalHistoryService.getMedicalConditions(patientId);
        return ResponseEntity.ok(ResponseModel.success(conditions, "Medical conditions retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/recent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientMedicalHistoryDto>>> getRecentMedicalHistory(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "30") int daysBack) {
        List<PatientMedicalHistoryDto> history = patientMedicalHistoryService.getRecentMedicalHistory(patientId, daysBack);
        return ResponseEntity.ok(ResponseModel.success(history, "Recent medical history retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/chronic")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<String>>> getChronicConditions(@PathVariable UUID patientId) {
        List<String> conditions = patientMedicalHistoryService.getChronicConditions(patientId);
        return ResponseEntity.ok(ResponseModel.success(conditions, "Chronic conditions retrieved successfully"));
    }

    // Statistics endpoints
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Object>> getMedicalHistoryStatistics() {
        return ResponseEntity.ok(ResponseModel.success(
                new Object() {
                    public final long totalHistory = patientMedicalHistoryService.countAll();
                    public final List<Object[]> conditionStats = patientMedicalHistoryService.getConditionStatistics();
                    public final List<String> distinctConditions = patientMedicalHistoryService.getDistinctConditions();
                    public final List<UUID> patientsWithMultipleRecords = patientMedicalHistoryService.findPatientsWithMultipleHistoryRecords();
                    public final List<UUID> patientsWithoutHistory = patientMedicalHistoryService.findPatientsWithoutMedicalHistory();
                },
                "Medical history statistics retrieved successfully"
        ));
    }

    // Bulk operations
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientMedicalHistoryDto>>> createBatch(
            @Valid @RequestBody List<PatientMedicalHistoryDto> patientMedicalHistoryDtos) {
        List<PatientMedicalHistoryDto> createdHistory = patientMedicalHistoryService.createBatch(patientMedicalHistoryDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdHistory, "Batch medical history created successfully"));
    }

    @DeleteMapping("/patient/{patientId}/all")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> deleteAllMedicalHistoryForPatient(@PathVariable UUID patientId) {
        patientMedicalHistoryService.deleteByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success("All medical history deleted for patient"));
    }
}
