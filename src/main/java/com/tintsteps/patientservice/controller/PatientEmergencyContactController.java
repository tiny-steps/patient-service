package com.tintsteps.patientservice.controller;

import com.tintsteps.patientservice.dto.PatientEmergencyContactDto;
import com.tintsteps.patientservice.model.ResponseModel;
import com.tintsteps.patientservice.service.PatientEmergencyContactService;
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
@RequestMapping("/api/v1/patient-emergency-contacts")
@RequiredArgsConstructor
public class PatientEmergencyContactController {

    private final PatientEmergencyContactService patientEmergencyContactService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientEmergencyContactDto.patientId)")
    public ResponseEntity<ResponseModel<PatientEmergencyContactDto>> createEmergencyContact(
            @Valid @RequestBody PatientEmergencyContactDto patientEmergencyContactDto) {
        PatientEmergencyContactDto createdContact = patientEmergencyContactService.create(patientEmergencyContactDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdContact, "Emergency contact created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientEmergencyContactOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientEmergencyContactDto>> getEmergencyContactById(@PathVariable UUID id) {
        PatientEmergencyContactDto contact = patientEmergencyContactService.findById(id);
        return ResponseEntity.ok(ResponseModel.success(contact, "Emergency contact retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientEmergencyContactDto>>> getAllEmergencyContacts(Pageable pageable) {
        Page<PatientEmergencyContactDto> contacts = patientEmergencyContactService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.success(contacts, "Emergency contacts retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientEmergencyContactOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientEmergencyContactDto>> updateEmergencyContact(
            @PathVariable UUID id, @Valid @RequestBody PatientEmergencyContactDto patientEmergencyContactDto) {
        PatientEmergencyContactDto updatedContact = patientEmergencyContactService.update(id, patientEmergencyContactDto);
        return ResponseEntity.ok(ResponseModel.success(updatedContact, "Emergency contact updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientEmergencyContactOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<Void>> deleteEmergencyContact(@PathVariable UUID id) {
        patientEmergencyContactService.delete(id);
        return ResponseEntity.ok(ResponseModel.success("Emergency contact deleted successfully"));
    }

    // Search endpoints
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientEmergencyContactDto>>> getContactsByPatientId(@PathVariable UUID patientId) {
        List<PatientEmergencyContactDto> contacts = patientEmergencyContactService.findByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success(contacts, "Emergency contacts retrieved successfully"));
    }

    @GetMapping("/relationship/{relationship}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientEmergencyContactDto>>> getContactsByRelationship(
            @PathVariable String relationship, Pageable pageable) {
        Page<PatientEmergencyContactDto> contacts = patientEmergencyContactService.findByRelationship(relationship, pageable);
        return ResponseEntity.ok(ResponseModel.success(contacts, "Emergency contacts retrieved successfully"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientEmergencyContactDto>>> searchContacts(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String relationship,
            @RequestParam(required = false) String phone,
            Pageable pageable) {
        Page<PatientEmergencyContactDto> contacts = patientEmergencyContactService.searchContacts(
                patientId, name, relationship, phone, pageable);
        return ResponseEntity.ok(ResponseModel.success(contacts, "Contact search completed successfully"));
    }

    @PutMapping("/{id}/update-info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientEmergencyContactOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientEmergencyContactDto>> updateContactInfo(
            @PathVariable UUID id,
            @RequestParam String name,
            @RequestParam String relationship,
            @RequestParam String phone) {
        PatientEmergencyContactDto contact = patientEmergencyContactService.updateContactInfo(id, name, relationship, phone);
        return ResponseEntity.ok(ResponseModel.success(contact, "Contact information updated successfully"));
    }

    // Business operations
    @PostMapping("/patient/{patientId}/add")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientEmergencyContactDto>> addEmergencyContact(
            @PathVariable UUID patientId,
            @RequestParam String name,
            @RequestParam String relationship,
            @RequestParam String phone) {
        PatientEmergencyContactDto contact = patientEmergencyContactService.addEmergencyContact(patientId, name, relationship, phone);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(contact, "Emergency contact added successfully"));
    }

    @DeleteMapping("/patient/{patientId}/remove")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> removeEmergencyContact(
            @PathVariable UUID patientId,
            @RequestParam String phone) {
        patientEmergencyContactService.removeEmergencyContact(patientId, phone);
        return ResponseEntity.ok(ResponseModel.success("Emergency contact removed successfully"));
    }

    @GetMapping("/patient/{patientId}/check/{phone}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Boolean>> hasEmergencyContact(
            @PathVariable UUID patientId, @PathVariable String phone) {
        boolean hasContact = patientEmergencyContactService.hasEmergencyContact(patientId, phone);
        return ResponseEntity.ok(ResponseModel.success(hasContact, "Emergency contact check completed"));
    }

    @GetMapping("/patient/{patientId}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Integer>> getEmergencyContactCount(@PathVariable UUID patientId) {
        int count = patientEmergencyContactService.getEmergencyContactCount(patientId);
        return ResponseEntity.ok(ResponseModel.success(count, "Emergency contact count retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/phones")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<String>>> getEmergencyContactPhones(@PathVariable UUID patientId) {
        List<String> phones = patientEmergencyContactService.getEmergencyContactPhones(patientId);
        return ResponseEntity.ok(ResponseModel.success(phones, "Emergency contact phones retrieved successfully"));
    }

    // Statistics endpoints
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Object>> getEmergencyContactStatistics() {
        return ResponseEntity.ok(ResponseModel.success(
                new Object() {
                    public final long totalContacts = patientEmergencyContactService.countAll();
                    public final List<Object[]> relationshipStats = patientEmergencyContactService.getRelationshipStatistics();
                    public final List<String> distinctRelationships = patientEmergencyContactService.getDistinctRelationships();
                    public final List<UUID> patientsWithMultipleContacts = patientEmergencyContactService.findPatientsWithMultipleContacts();
                    public final List<UUID> patientsWithoutContacts = patientEmergencyContactService.findPatientsWithoutEmergencyContacts();
                },
                "Emergency contact statistics retrieved successfully"
        ));
    }

    // Bulk operations
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientEmergencyContactDto>>> createBatch(
            @Valid @RequestBody List<PatientEmergencyContactDto> patientEmergencyContactDtos) {
        List<PatientEmergencyContactDto> createdContacts = patientEmergencyContactService.createBatch(patientEmergencyContactDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdContacts, "Batch emergency contacts created successfully"));
    }

    @DeleteMapping("/patient/{patientId}/all")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> deleteAllContactsForPatient(@PathVariable UUID patientId) {
        patientEmergencyContactService.deleteByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success("All emergency contacts deleted for patient"));
    }
}
