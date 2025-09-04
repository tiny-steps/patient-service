package com.tinysteps.patientservice.controller;

import com.tinysteps.patientservice.dto.PatientAddressDto;
import com.tinysteps.patientservice.model.ResponseModel;
import com.tinysteps.patientservice.service.PatientAddressService;
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
@RequestMapping("/api/v1/patient-addresses")
@RequiredArgsConstructor
public class PatientAddressController {

    private final PatientAddressService patientAddressService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientAddressDto.patientId)")
    public ResponseEntity<ResponseModel<PatientAddressDto>> createPatientAddress(
            @Valid @RequestBody PatientAddressDto patientAddressDto) {
        PatientAddressDto createdAddress = patientAddressService.create(patientAddressDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdAddress, "Patient address created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientAddressOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientAddressDto>> getPatientAddressById(@PathVariable UUID id) {
        PatientAddressDto address = patientAddressService.findById(id);
        return ResponseEntity.ok(ResponseModel.success(address, "Patient address retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientAddressDto>>> getAllPatientAddresses(Pageable pageable) {
        Page<PatientAddressDto> addresses = patientAddressService.findAll(pageable);
        return ResponseEntity.ok(ResponseModel.success(addresses, "Patient addresses retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientAddressOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<PatientAddressDto>> updatePatientAddress(
            @PathVariable UUID id, @Valid @RequestBody PatientAddressDto patientAddressDto) {
        PatientAddressDto updatedAddress = patientAddressService.update(id, patientAddressDto);
        return ResponseEntity.ok(ResponseModel.success(updatedAddress, "Patient address updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientAddressOwner(authentication, #id)")
    public ResponseEntity<ResponseModel<Void>> deletePatientAddress(@PathVariable UUID id) {
        patientAddressService.delete(id);
        return ResponseEntity.ok(ResponseModel.success("Patient address deleted successfully"));
    }

    // Search endpoints
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<PatientAddressDto>>> getAddressesByPatientId(@PathVariable UUID patientId) {
        List<PatientAddressDto> addresses = patientAddressService.findByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success(addresses, "Patient addresses retrieved successfully"));
    }

    @GetMapping("/address/{addressId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientAddressDto>>> getPatientsByAddressId(@PathVariable UUID addressId) {
        List<PatientAddressDto> addresses = patientAddressService.findByAddressId(addressId);
        return ResponseEntity.ok(ResponseModel.success(addresses, "Patients for address retrieved successfully"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientAddressDto>>> searchAddresses(
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID addressId,
            Pageable pageable) {
        Page<PatientAddressDto> addresses = patientAddressService.searchAddresses(patientId, addressId, pageable);
        return ResponseEntity.ok(ResponseModel.success(addresses, "Address search completed successfully"));
    }

    // Business operations
    @PostMapping("/patient/{patientId}/link/{addressId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientAddressDto>> linkPatientToAddress(
            @PathVariable UUID patientId, @PathVariable UUID addressId) {
        PatientAddressDto address = patientAddressService.linkPatientToAddress(patientId, addressId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(address, "Patient linked to address successfully"));
    }

    @DeleteMapping("/patient/{patientId}/unlink/{addressId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> unlinkPatientFromAddress(
            @PathVariable UUID patientId, @PathVariable UUID addressId) {
        patientAddressService.unlinkPatientFromAddress(patientId, addressId);
        return ResponseEntity.ok(ResponseModel.success("Patient unlinked from address successfully"));
    }

    @GetMapping("/patient/{patientId}/check/{addressId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Boolean>> isPatientLinkedToAddress(
            @PathVariable UUID patientId, @PathVariable UUID addressId) {
        boolean isLinked = patientAddressService.isPatientLinkedToAddress(patientId, addressId);
        return ResponseEntity.ok(ResponseModel.success(isLinked, "Link check completed"));
    }

    @GetMapping("/patient/{patientId}/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Integer>> getAddressCount(@PathVariable UUID patientId) {
        int count = patientAddressService.getAddressCount(patientId);
        return ResponseEntity.ok(ResponseModel.success(count, "Address count retrieved successfully"));
    }

    @GetMapping("/patient/{patientId}/address-ids")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<List<UUID>>> getAddressIds(@PathVariable UUID patientId) {
        List<UUID> addressIds = patientAddressService.getAddressIds(patientId);
        return ResponseEntity.ok(ResponseModel.success(addressIds, "Address IDs retrieved successfully"));
    }

    // Statistics endpoints
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Object>> getAddressStatistics() {
        return ResponseEntity.ok(ResponseModel.success(
                new Object() {
                    public final long totalAddresses = patientAddressService.countAll();
                    public final long distinctAddresses = patientAddressService.countDistinctAddresses();
                    public final long distinctPatients = patientAddressService.countDistinctPatients();
                    public final List<Object[]> addressUsageStats = patientAddressService.getAddressUsageStatistics();
                    public final List<UUID> patientsWithMultipleAddresses = patientAddressService.findPatientsWithMultipleAddresses();
                    public final List<UUID> addressesUsedByMultiplePatients = patientAddressService.findAddressesUsedByMultiplePatients();
                },
                "Address statistics retrieved successfully"
        ));
    }

    // Bulk operations
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientAddressDto>>> createBatch(
            @Valid @RequestBody List<PatientAddressDto> patientAddressDtos) {
        List<PatientAddressDto> createdAddresses = patientAddressService.createBatch(patientAddressDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseModel.created(createdAddresses, "Batch addresses created successfully"));
    }

    @DeleteMapping("/patient/{patientId}/all")
    @PreAuthorize("hasRole('ADMIN') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<Void>> deleteAllAddressesForPatient(@PathVariable UUID patientId) {
        patientAddressService.deleteByPatientId(patientId);
        return ResponseEntity.ok(ResponseModel.success("All addresses deleted for patient"));
    }

    @DeleteMapping("/address/{addressId}/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseModel<Void>> deleteAllPatientsForAddress(@PathVariable UUID addressId) {
        patientAddressService.deleteByAddressId(addressId);
        return ResponseEntity.ok(ResponseModel.success("All patient links deleted for address"));
    }
}
