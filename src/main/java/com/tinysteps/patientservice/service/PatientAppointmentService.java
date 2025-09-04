package com.tinysteps.patientservice.service;

import com.tinysteps.patientservice.dto.PatientAppointmentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    Page<PatientAppointmentDto> findByPatientId(UUID patientId, Pageable pageable);

    Page<PatientAppointmentDto> searchAppointments(UUID patientId, UUID appointmentId, Pageable pageable);

    // Business Operations - Only keep used methods
    PatientAppointmentDto linkPatientToAppointment(UUID patientId, UUID appointmentId);
    void unlinkPatientFromAppointment(UUID patientId, UUID appointmentId);


    PatientAppointmentDto getPatientForAppointment(UUID appointmentId);
    boolean isPatientLinkedToAppointment(UUID patientId, UUID appointmentId);

    // Validation Operations
    @Transactional(readOnly = true)
    boolean existsById(UUID id);

    @Transactional(readOnly = true)
    boolean existsByPatientId(UUID patientId);


    @Transactional(readOnly = true)
    boolean existsByPatientIdAndAppointmentId(UUID patientId, UUID appointmentId);

    // Statistics Operations
    @Transactional(readOnly = true)
    long countByPatientId(UUID patientId);

    // Statistics Operations - Only keep used methods
    long countAll();

    @Transactional(readOnly = true)
    long countDistinctAppointments();

    @Transactional(readOnly = true)
    long countDistinctPatients();

    List<Object[]> getPatientAppointmentStatistics();

    @Transactional(readOnly = true)
    List<UUID> findPatientsWithMultipleAppointments();

    @Transactional(readOnly = true)
    List<UUID> findAppointmentsWithMultiplePatients();

    // Bulk Operations - Only keep used methods
    List<PatientAppointmentDto> createBatch(List<PatientAppointmentDto> patientAppointmentDtos);
    void deleteByPatientId(UUID patientId);

    @Transactional
    void deleteByAppointmentId(UUID appointmentId);

    @Transactional
    void deleteBatch(List<UUID> ids);

    // Appointment Management
    @Transactional(readOnly = true)
    int getAppointmentCount(UUID patientId);


    @Transactional(readOnly = true)
    List<UUID> getAppointmentIds(UUID patientId);

}
