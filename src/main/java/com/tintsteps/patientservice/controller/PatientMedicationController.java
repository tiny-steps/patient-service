package com.tintsteps.patientservice.controller;

import com.tintsteps.patientservice.dto.PatientMedicationDto;
import com.tintsteps.patientservice.model.ResponseModel;
import com.tintsteps.patientservice.service.PatientMedicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/patient-medications")
@RequiredArgsConstructor
public class PatientMedicationController {

    private final PatientMedicationService patientMedicationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientMedicationDto.patientId)")
    public ResponseEntity<ResponseModel<PatientMedicationDto>> createPatientMedication(
            @Valid @RequestBody PatientMedicationDto patientMedicationDto) {
        PatientMedicationDto createdMedication = patientMedicationService.create(patientMedicationDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdMedication, "Patient medication created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientMedicationOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientMedicationDto>> getPatientMedicationById(@PathVariable UUID id) {
        PatientMedicationDto medication = patientMedicationService.findById(id);
        return ResponseEntity.ok(ResponseModel.success(medication, "Patient medication retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientMedicationDto>>> getAllPatientMedications(Pageable pageable) {
        Page<PatientMedicationDto> medications = patientMedicationService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.success(medications, "Patient medications retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientMedicationOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientMedicationDto>> updatePatientMedication(
            @PathVariable UUID id, @Valid @RequestBody PatientMedicationDto patientMedicationDto) {
        PatientMedicationDto updatedMedication = patientMedicationService.update(id, patientMedicationDto);
        return ResponseEntity.ok(ResponseModel.success(updatedMedication, "Patient medication updated successfully"));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientMedicationOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientMedicationDto>> partialUpdatePatientMedication(
            @PathVariable UUID id, @RequestBody PatientMedicationDto patientMedicationDto) {
        PatientMedicationDto updatedMedication = patientMedicationService.partialUpdate(id, patientMedicationDto);
        return ResponseEntity.ok(ResponseModel.success(updatedMedication, "Patient medication updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientMedicationOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<Void>> deletePatientMedication(@PathVariable UUID id) {
        patientMedicationService.delete(id);
        return ResponseEntity.ok(ResponseModel.success("Patient medication deleted successfully"));
    }

    // Search endpoints
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientMedicationDto>>> getMedicationsByPatientId(@PathVariable UUID patientId) {
        List<PatientMedicationDto> medications = patientMedicationService.findByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success(medications, "Patient medications retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/paginated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Page<PatientMedicationDto>>> getMedicationsByPatientIdPaginated(
            @PathVariable UUID patientId, Pageable pageable) {
        Page<PatientMedicationDto> medications = patientMedicationService.findByPatientId(patientId, pageable);
        return ResponseEntity.ok(ResponseModel.success(medications, "Patient medications retrieved successfully"));
    }

    @GetMapping("/medication/{medicationName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientMedicationDto>>> getMedicationsByName(
            @PathVariable String medicationName, Pageable pageable) {
        Page<PatientMedicationDto> medications = patientMedicationService.findByMedicationName(medicationName, pageable);
        return ResponseEntity.ok(ResponseModel.success(medications, "Medications retrieved successfully"));
    }

    @GetMapping("/dosage/{dosage}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientMedicationDto>>> getMedicationsByDosage(
            @PathVariable String dosage, Pageable pageable) {
        Page<PatientMedicationDto> medications = patientMedicationService.findByDosage(dosage, pageable);
        return ResponseEntity.ok(ResponseModel.success(medications, "Medications retrieved successfully"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientMedicationDto>>> searchMedications(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) String medicationName,
            @RequestParam(required = false) String dosage,
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate,
            Pageable pageable) {
        Page<PatientMedicationDto> medications = patientMedicationService.searchMedications(
                patientId, medicationName, dosage, startDate, endDate, pageable);
        return ResponseEntity.ok(ResponseModel.success(medications, "Medications search completed successfully"));
    }

    // Business operations
    @PostMapping("/patient/{patientId}/add")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientMedicationDto>> addMedicationToPatient(
            @PathVariable UUID patientId,
            @RequestParam String medicationName,
            @RequestParam(required = false) String dosage,
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate) {
        PatientMedicationDto medication = patientMedicationService.addMedication(patientId, medicationName, dosage, startDate, endDate);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(medication, "Medication added successfully"));
    }

    @PostMapping("/patient/{patientId}/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientMedicationDto>> startMedication(
            @PathVariable UUID patientId,
            @RequestParam String medicationName,
            @RequestParam String dosage) {
        PatientMedicationDto medication = patientMedicationService.startMedication(patientId, medicationName, dosage);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(medication, "Medication started successfully"));
    }

    @PutMapping("/patient/{patientId}/stop")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientMedicationDto>> stopMedication(
            @PathVariable UUID patientId,
            @RequestParam String medicationName,
            @RequestParam Date endDate) {
        PatientMedicationDto medication = patientMedicationService.stopMedication(patientId, medicationName, endDate);
        return ResponseEntity.ok(ResponseModel.success(medication, "Medication stopped successfully"));
    }

    @DeleteMapping("/patient/{patientId}/discontinue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> discontinueMedication(
            @PathVariable UUID patientId,
            @RequestParam String medicationName) {
        patientMedicationService.discontinueMedication(patientId, medicationName);
        return ResponseEntity.ok(ResponseModel.success("Medication discontinued successfully"));
    }

    @GetMapping("/patient/{patientId}/current")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientMedicationDto>>> getCurrentMedications(@PathVariable UUID patientId) {
        List<PatientMedicationDto> medications = patientMedicationService.getCurrentMedicationsForPatient(patientId);
        return ResponseEntity.ok(ResponseModel.success(medications, "Current medications retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/active-names")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<String>>> getActiveMedicationNames(@PathVariable UUID patientId) {
        List<String> medicationNames = patientMedicationService.getActiveMedicationNames(patientId);
        return ResponseEntity.ok(ResponseModel.success(medicationNames, "Active medication names retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientMedicationDto>>> getMedicationHistory(@PathVariable UUID patientId) {
        List<PatientMedicationDto> medications = patientMedicationService.getMedicationHistory(patientId);
        return ResponseEntity.ok(ResponseModel.success(medications, "Medication history retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/expiring")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientMedicationDto>>> getExpiringMedications(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<PatientMedicationDto> medications = patientMedicationService.getExpiringMedications(patientId, daysAhead);
        return ResponseEntity.ok(ResponseModel.success(medications, "Expiring medications retrieved successfully"));
    }

    // Medication management
    @PutMapping("/{medicationId}/renew")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientMedicationOwner(authentication, #medicationId)")
    public ResponseEntity<ResponseModel<PatientMedicationDto>> renewMedication(
            @PathVariable UUID medicationId,
            @RequestParam Date newEndDate) {
        PatientMedicationDto medication = patientMedicationService.renewMedication(medicationId, newEndDate);
        return ResponseEntity.ok(ResponseModel.success(medication, "Medication renewed successfully"));
    }

    @PutMapping("/{medicationId}/change-dosage")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientMedicationOwner(authentication, #medicationId)")
    public ResponseEntity<ResponseModel<PatientMedicationDto>> changeDosage(
            @PathVariable UUID medicationId,
            @RequestParam String newDosage) {
        PatientMedicationDto medication = patientMedicationService.changeDosage(medicationId, newDosage);
        return ResponseEntity.ok(ResponseModel.success(medication, "Dosage changed successfully"));
    }

    // Validation endpoints
    @GetMapping("/patient/{patientId}/check/{medicationName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Boolean>> isOnMedication(
            @PathVariable UUID patientId,
            @PathVariable String medicationName) {
        boolean isOnMedication = patientMedicationService.isOnMedication(patientId, medicationName);
        return ResponseEntity.ok(ResponseModel.success(isOnMedication, "Medication check completed"));
    }

    @GetMapping("/patient/{patientId}/conflicts/{medicationName}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Boolean>> checkMedicationConflicts(
            @PathVariable UUID patientId,
            @PathVariable String medicationName) {
        boolean hasConflicts = patientMedicationService.hasMedicationConflicts(patientId, medicationName);
        return ResponseEntity.ok(ResponseModel.success(hasConflicts, "Medication conflict check completed"));
    }

    // Statistics endpoints
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Object>> getMedicationStatistics() {
        return ResponseEntity.ok(ResponseModel.success(
                new Object() {
                    public final long totalMedications = patientMedicationService.countAll();
                    public final List<Object[]> medicationStats = patientMedicationService.getMedicationStatistics();
                    public final List<String> distinctMedications = patientMedicationService.getDistinctMedicationNames();
                    public final List<String> distinctDosages = patientMedicationService.getDistinctDosages();
                },
                "Medication statistics retrieved successfully"
        ));
    }

    // Bulk operations
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientMedicationDto>>> createBatch(
            @Valid @RequestBody List<PatientMedicationDto> patientMedicationDtos) {
        List<PatientMedicationDto> createdMedications = patientMedicationService.createBatch(patientMedicationDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdMedications, "Batch medications created successfully"));
    }

    @DeleteMapping("/patient/{patientId}/all")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> deleteAllMedicationsForPatient(@PathVariable UUID patientId) {
        patientMedicationService.deleteByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success("All medications deleted for patient"));
    }
}
