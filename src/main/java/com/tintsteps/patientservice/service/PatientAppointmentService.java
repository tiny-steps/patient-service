package com.tintsteps.patientservice.service;

import com.tintsteps.patientservice.dto.PatientAppointmentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PatientAppointmentService {

    // CRUD Operations
    PatientAppointmentDto create(PatientAppointmentDto patientAppointmentDto);
    PatientAppointmentDto findById(UUID id);
    List<PatientAppointmentDto> findByPatientId(UUID patientId);
    Page<PatientAppointmentDto> findAll(Pageable pageable);
    PatientAppointmentDto update(UUID id, PatientAppointmentDto patientAppointmentDto);
    PatientAppointmentDto partialUpdate(UUID id, PatientAppointmentDto patientAppointmentDto);
    void delete(UUID id);

    // Search Operations - Only keep used methods
    List<PatientAppointmentDto> findByAppointmentId(UUID appointmentId);
    Page<PatientAppointmentDto> searchAppointments(UUID patientId, UUID appointmentId, Pageable pageable);

    // Business Operations - Only keep used methods
    PatientAppointmentDto linkPatientToAppointment(UUID patientId, UUID appointmentId);
    void unlinkPatientFromAppointment(UUID patientId, UUID appointmentId);
    PatientAppointmentDto getPatientForAppointment(UUID appointmentId);
    boolean isPatientLinkedToAppointment(UUID patientId, UUID appointmentId);

    // Statistics Operations - Only keep used methods
    long countAll();
    List<Object[]> getPatientAppointmentStatistics();

    // Bulk Operations - Only keep used methods
    List<PatientAppointmentDto> createBatch(List<PatientAppointmentDto> patientAppointmentDtos);
    void deleteByPatientId(UUID patientId);
}
