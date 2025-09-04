package com.tinysteps.patientservice.controller;

import com.tinysteps.patientservice.dto.PatientAppointmentDto;
import com.tinysteps.patientservice.model.ResponseModel;
import com.tinysteps.patientservice.service.PatientAppointmentService;
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
@RequestMapping("/api/v1/patient-appointments")
@RequiredArgsConstructor
public class PatientAppointmentController {

    private final PatientAppointmentService patientAppointmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientAppointmentDto.patientId)")
    public ResponseEntity<ResponseModel<PatientAppointmentDto>> createPatientAppointment(
            @Valid @RequestBody PatientAppointmentDto patientAppointmentDto) {
        PatientAppointmentDto createdAppointment = patientAppointmentService.create(patientAppointmentDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdAppointment, "Patient appointment created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientAppointmentOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientAppointmentDto>> getPatientAppointmentById(@PathVariable UUID id) {
        PatientAppointmentDto appointment = patientAppointmentService.findById(id);
        return ResponseEntity.ok(ResponseModel.success(appointment, "Patient appointment retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientAppointmentDto>>> getAllPatientAppointments(Pageable pageable) {
        Page<PatientAppointmentDto> appointments = patientAppointmentService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.success(appointments, "Patient appointments retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientAppointmentOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientAppointmentDto>> updatePatientAppointment(
            @PathVariable UUID id, @Valid @RequestBody PatientAppointmentDto patientAppointmentDto) {
        PatientAppointmentDto updatedAppointment = patientAppointmentService.update(id, patientAppointmentDto);
        return ResponseEntity.ok(ResponseModel.success(updatedAppointment, "Patient appointment updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientAppointmentOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<Void>> deletePatientAppointment(@PathVariable UUID id) {
        patientAppointmentService.delete(id);
        return ResponseEntity.ok(ResponseModel.success("Patient appointment deleted successfully"));
    }

    // Search endpoints
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientAppointmentDto>>> getAppointmentsByPatientId(@PathVariable UUID patientId) {
        List<PatientAppointmentDto> appointments = patientAppointmentService.findByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success(appointments, "Patient appointments retrieved successfully"));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientAppointmentDto>>> getPatientsByAppointmentId(@PathVariable UUID appointmentId) {
        List<PatientAppointmentDto> appointments = patientAppointmentService.findByAppointmentId(appointmentId);
        return ResponseEntity.ok(ResponseModel.success(appointments, "Patients for appointment retrieved successfully"));
    }

    @GetMapping("/appointment/{appointmentId}/patient")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<PatientAppointmentDto>> getPatientForAppointment(@PathVariable UUID appointmentId) {
        PatientAppointmentDto appointment = patientAppointmentService.getPatientForAppointment(appointmentId);
        return ResponseEntity.ok(ResponseModel.success(appointment, "Patient for appointment retrieved successfully"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientAppointmentDto>>> searchAppointments(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID appointmentId,
            Pageable pageable) {
        Page<PatientAppointmentDto> appointments = patientAppointmentService.searchAppointments(patientId, appointmentId, pageable);
        return ResponseEntity.ok(ResponseModel.success(appointments, "Appointment search completed successfully"));
    }

    // Business operations
    @PostMapping("/patient/{patientId}/link/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientAppointmentDto>> linkPatientToAppointment(
            @PathVariable UUID patientId, @PathVariable UUID appointmentId) {
        PatientAppointmentDto appointment = patientAppointmentService.linkPatientToAppointment(patientId, appointmentId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(appointment, "Patient linked to appointment successfully"));
    }

    @DeleteMapping("/patient/{patientId}/unlink/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> unlinkPatientFromAppointment(
            @PathVariable UUID patientId, @PathVariable UUID appointmentId) {
        patientAppointmentService.unlinkPatientFromAppointment(patientId, appointmentId);
        return ResponseEntity.ok(ResponseModel.success("Patient unlinked from appointment successfully"));
    }

    @GetMapping("/patient/{patientId}/check/{appointmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Boolean>> isPatientLinkedToAppointment(
            @PathVariable UUID patientId, @PathVariable UUID appointmentId) {
        boolean isLinked = patientAppointmentService.isPatientLinkedToAppointment(patientId, appointmentId);
        return ResponseEntity.ok(ResponseModel.success(isLinked, "Link check completed"));
    }

    @GetMapping("/patient/{patientId}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Integer>> getAppointmentCount(@PathVariable UUID patientId) {
        int count = patientAppointmentService.getAppointmentCount(patientId);
        return ResponseEntity.ok(ResponseModel.success(count, "Appointment count retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/appointment-ids")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<UUID>>> getAppointmentIds(@PathVariable UUID patientId) {
        List<UUID> appointmentIds = patientAppointmentService.getAppointmentIds(patientId);
        return ResponseEntity.ok(ResponseModel.success(appointmentIds, "Appointment IDs retrieved successfully"));
    }

    // Statistics endpoints
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Object>> getAppointmentStatistics() {
        return ResponseEntity.ok(ResponseModel.success(
                new Object() {
                    public final long totalAppointments = patientAppointmentService.countAll();
                    public final long distinctAppointments = patientAppointmentService.countDistinctAppointments();
                    public final long distinctPatients = patientAppointmentService.countDistinctPatients();
                    public final List<Object[]> appointmentStats = patientAppointmentService.getPatientAppointmentStatistics();
                    public final List<UUID> patientsWithMultipleAppointments = patientAppointmentService.findPatientsWithMultipleAppointments();
                    public final List<UUID> appointmentsWithMultiplePatients = patientAppointmentService.findAppointmentsWithMultiplePatients();
                },
                "Appointment statistics retrieved successfully"
        ));
    }

    // Bulk operations
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientAppointmentDto>>> createBatch(
            @Valid @RequestBody List<PatientAppointmentDto> patientAppointmentDtos) {
        List<PatientAppointmentDto> createdAppointments = patientAppointmentService.createBatch(patientAppointmentDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdAppointments, "Batch appointments created successfully"));
    }

    @DeleteMapping("/patient/{patientId}/all")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> deleteAllAppointmentsForPatient(@PathVariable UUID patientId) {
        patientAppointmentService.deleteByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success("All appointments deleted for patient"));
    }

    @DeleteMapping("/appointment/{appointmentId}/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteAllPatientsForAppointment(@PathVariable UUID appointmentId) {
        patientAppointmentService.deleteByAppointmentId(appointmentId);
        return ResponseEntity.ok(ResponseModel.success("All patient links deleted for appointment"));
    }
}
