package com.tintsteps.patientservice.service.impl;

import com.tintsteps.patientservice.dto.PatientAppointmentDto;
import com.tintsteps.patientservice.exception.PatientNotFoundException;
import com.tintsteps.patientservice.exception.PatientServiceException;
import com.tintsteps.patientservice.mapper.PatientAppointmentMapper;
import com.tintsteps.patientservice.model.Patient;
import com.tintsteps.patientservice.model.PatientAppointment;
import com.tintsteps.patientservice.repository.PatientAppointmentRepository;
import com.tintsteps.patientservice.repository.PatientRepository;
import com.tintsteps.patientservice.service.PatientAppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientAppointmentServiceImpl implements PatientAppointmentService {

    private final PatientAppointmentRepository patientAppointmentRepository;
    private final PatientRepository patientRepository;
    private final PatientAppointmentMapper patientAppointmentMapper = PatientAppointmentMapper.INSTANCE;

    @Override
    @Transactional
    public PatientAppointmentDto create(PatientAppointmentDto patientAppointmentDto) {
        log.info("Creating patient appointment for patient ID: {}", patientAppointmentDto.getPatientId());
        
        try {
            if (patientAppointmentDto.getPatientId() == null) {
                throw new IllegalArgumentException("Patient ID is required");
            }
            
            if (patientAppointmentDto.getAppointmentId() == null) {
                throw new IllegalArgumentException("Appointment ID is required");
            }
            
            // Verify patient exists
            Patient patient = patientRepository.findById(patientAppointmentDto.getPatientId())
                    .orElseThrow(() -> new PatientNotFoundException(patientAppointmentDto.getPatientId()));
            
            // Check if link already exists
            if (existsByPatientIdAndAppointmentId(patientAppointmentDto.getPatientId(), patientAppointmentDto.getAppointmentId())) {
                throw new PatientServiceException("Patient is already linked to this appointment");
            }
            
            PatientAppointment patientAppointment = patientAppointmentMapper.patientAppointmentDtoToPatientAppointment(patientAppointmentDto);
            patientAppointment.setPatient(patient);
            
            PatientAppointment savedAppointment = patientAppointmentRepository.save(patientAppointment);
            
            log.info("Patient appointment created successfully with ID: {}", savedAppointment.getId());
            return patientAppointmentMapper.patientAppointmentToPatientAppointmentDto(savedAppointment);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating patient appointment: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create patient appointment", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PatientAppointmentDto findById(UUID id) {
        log.info("Finding patient appointment by ID: {}", id);
        
        PatientAppointment patientAppointment = patientAppointmentRepository.findById(id)
                .orElseThrow(() -> new PatientServiceException("Patient appointment not found with id: " + id));
        
        return patientAppointmentMapper.patientAppointmentToPatientAppointmentDto(patientAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientAppointmentDto> findByPatientId(UUID patientId) {
        log.info("Finding patient appointments by patient ID: {}", patientId);
        
        List<PatientAppointment> appointments = patientAppointmentRepository.findByPatientId(patientId);
        return appointments.stream()
                .map(patientAppointmentMapper::patientAppointmentToPatientAppointmentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientAppointmentDto> findAll(Pageable pageable) {
        log.info("Finding all patient appointments with pagination");
        
        Page<PatientAppointment> appointments = patientAppointmentRepository.findAll(pageable);
        return appointments.map(patientAppointmentMapper::patientAppointmentToPatientAppointmentDto);
    }

    @Override
    @Transactional
    public PatientAppointmentDto update(UUID id, PatientAppointmentDto patientAppointmentDto) {
        log.info("Updating patient appointment with ID: {}", id);
        
        try {
            PatientAppointment existingAppointment = patientAppointmentRepository.findById(id)
                    .orElseThrow(() -> new PatientServiceException("Patient appointment not found with id: " + id));
            
            // Update appointment ID if provided
            if (patientAppointmentDto.getAppointmentId() != null) {
                existingAppointment.setAppointmentId(patientAppointmentDto.getAppointmentId());
            }
            
            PatientAppointment updatedAppointment = patientAppointmentRepository.save(existingAppointment);
            return patientAppointmentMapper.patientAppointmentToPatientAppointmentDto(updatedAppointment);
        } catch (Exception e) {
            log.error("Error updating patient appointment: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to update patient appointment", e);
        }
    }

    @Override
    @Transactional
    public PatientAppointmentDto partialUpdate(UUID id, PatientAppointmentDto patientAppointmentDto) {
        return update(id, patientAppointmentDto);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting patient appointment with ID: {}", id);
        
        try {
            if (!patientAppointmentRepository.existsById(id)) {
                throw new PatientServiceException("Patient appointment not found with id: " + id);
            }
            
            patientAppointmentRepository.deleteById(id);
            log.info("Patient appointment deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting patient appointment: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete patient appointment", e);
        }
    }

    // Search Operations
    @Override
    @Transactional(readOnly = true)
    public List<PatientAppointmentDto> findByAppointmentId(UUID appointmentId) {
        log.info("Finding patient appointments by appointment ID: {}", appointmentId);
        
        List<PatientAppointment> appointments = patientAppointmentRepository.findByAppointmentId(appointmentId);
        return appointments.stream()
                .map(patientAppointmentMapper::patientAppointmentToPatientAppointmentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientAppointmentDto> findByAppointmentId(UUID appointmentId, Pageable pageable) {
        log.info("Finding patient appointments by appointment ID: {} with pagination", appointmentId);
        
        Page<PatientAppointment> appointments = patientAppointmentRepository.findByAppointmentId(appointmentId, pageable);
        return appointments.map(patientAppointmentMapper::patientAppointmentToPatientAppointmentDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientAppointmentDto> findByPatientId(UUID patientId, Pageable pageable) {
        log.info("Finding patient appointments by patient ID: {} with pagination", patientId);
        
        Page<PatientAppointment> appointments = patientAppointmentRepository.findByPatientId(patientId, pageable);
        return appointments.map(patientAppointmentMapper::patientAppointmentToPatientAppointmentDto);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientAppointmentDto findByPatientAndAppointment(UUID patientId, UUID appointmentId) {
        log.info("Finding patient appointment by patient ID: {} and appointment ID: {}", patientId, appointmentId);
        
        PatientAppointment appointment = patientAppointmentRepository.findByPatientIdAndAppointmentId(patientId, appointmentId)
                .orElseThrow(() -> new PatientServiceException("Patient appointment not found"));
        
        return patientAppointmentMapper.patientAppointmentToPatientAppointmentDto(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientAppointmentDto> searchAppointments(UUID patientId, UUID appointmentId, Pageable pageable) {
        log.info("Searching patient appointments with multiple criteria");
        
        // For now, implement basic search - can be enhanced with Specifications
        Page<PatientAppointment> appointments = patientAppointmentRepository.findAll(pageable);
        return appointments.map(patientAppointmentMapper::patientAppointmentToPatientAppointmentDto);
    }

    // Business Operations
    @Override
    @Transactional
    public PatientAppointmentDto linkPatientToAppointment(UUID patientId, UUID appointmentId) {
        log.info("Linking patient ID: {} to appointment ID: {}", patientId, appointmentId);
        
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(patientId));
            
            if (existsByPatientIdAndAppointmentId(patientId, appointmentId)) {
                throw new PatientServiceException("Patient is already linked to this appointment");
            }
            
            PatientAppointment patientAppointment = new PatientAppointment();
            patientAppointment.setPatient(patient);
            patientAppointment.setAppointmentId(appointmentId);
            
            PatientAppointment savedAppointment = patientAppointmentRepository.save(patientAppointment);
            return patientAppointmentMapper.patientAppointmentToPatientAppointmentDto(savedAppointment);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error linking patient to appointment: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to link patient to appointment", e);
        }
    }

    @Override
    @Transactional
    public void unlinkPatientFromAppointment(UUID patientId, UUID appointmentId) {
        log.info("Unlinking patient ID: {} from appointment ID: {}", patientId, appointmentId);
        
        try {
            patientAppointmentRepository.deleteByPatientIdAndAppointmentId(patientId, appointmentId);
        } catch (Exception e) {
            log.error("Error unlinking patient from appointment: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to unlink patient from appointment", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientAppointmentDto> getAppointmentsForPatient(UUID patientId) {
        return findByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientAppointmentDto getPatientForAppointment(UUID appointmentId) {
        List<PatientAppointmentDto> appointments = findByAppointmentId(appointmentId);
        return appointments.isEmpty() ? null : appointments.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPatientLinkedToAppointment(UUID patientId, UUID appointmentId) {
        return patientAppointmentRepository.existsByPatientIdAndAppointmentId(patientId, appointmentId);
    }

    // Validation Operations
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return patientAppointmentRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPatientId(UUID patientId) {
        return patientAppointmentRepository.existsByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAppointmentId(UUID appointmentId) {
        return patientAppointmentRepository.existsByAppointmentId(appointmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPatientIdAndAppointmentId(UUID patientId, UUID appointmentId) {
        return patientAppointmentRepository.existsByPatientIdAndAppointmentId(patientId, appointmentId);
    }

    // Statistics Operations
    @Override
    @Transactional(readOnly = true)
    public long countByPatientId(UUID patientId) {
        return patientAppointmentRepository.countByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAppointmentId(UUID appointmentId) {
        return patientAppointmentRepository.countByAppointmentId(appointmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return patientAppointmentRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countDistinctAppointments() {
        return patientAppointmentRepository.countDistinctAppointments();
    }

    @Override
    @Transactional(readOnly = true)
    public long countDistinctPatients() {
        return patientAppointmentRepository.countDistinctPatients();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPatientAppointmentStatistics() {
        return patientAppointmentRepository.getPatientAppointmentStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findPatientsWithMultipleAppointments() {
        return patientAppointmentRepository.findPatientsWithMultipleAppointments();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findAppointmentsWithMultiplePatients() {
        return patientAppointmentRepository.findAppointmentsWithMultiplePatients();
    }

    // Bulk Operations
    @Override
    @Transactional
    public List<PatientAppointmentDto> createBatch(List<PatientAppointmentDto> patientAppointmentDtos) {
        log.info("Creating batch of {} patient appointments", patientAppointmentDtos.size());

        try {
            List<PatientAppointment> appointments = patientAppointmentDtos.stream()
                    .map(dto -> {
                        Patient patient = patientRepository.findById(dto.getPatientId())
                                .orElseThrow(() -> new PatientNotFoundException(dto.getPatientId()));
                        PatientAppointment appointment = patientAppointmentMapper.patientAppointmentDtoToPatientAppointment(dto);
                        appointment.setPatient(patient);
                        return appointment;
                    })
                    .collect(Collectors.toList());

            List<PatientAppointment> savedAppointments = patientAppointmentRepository.saveAll(appointments);
            return savedAppointments.stream()
                    .map(patientAppointmentMapper::patientAppointmentToPatientAppointmentDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error creating batch appointments: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create batch appointments", e);
        }
    }

    @Override
    @Transactional
    public void deleteByPatientId(UUID patientId) {
        log.info("Deleting all appointments for patient ID: {}", patientId);
        patientAppointmentRepository.deleteByPatientId(patientId);
    }

    @Override
    @Transactional
    public void deleteByAppointmentId(UUID appointmentId) {
        log.info("Deleting all patient links for appointment ID: {}", appointmentId);
        patientAppointmentRepository.deleteByAppointmentId(appointmentId);
    }

    @Override
    @Transactional
    public void deleteBatch(List<UUID> ids) {
        log.info("Deleting batch of {} patient appointments", ids.size());
        patientAppointmentRepository.deleteAllById(ids);
    }

    // Appointment Management
    @Override
    @Transactional(readOnly = true)
    public int getAppointmentCount(UUID patientId) {
        return (int) countByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMultipleAppointments(UUID patientId) {
        return countByPatientId(patientId) > 1;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getAppointmentIds(UUID patientId) {
        List<PatientAppointment> appointments = patientAppointmentRepository.findByPatientId(patientId);
        return appointments.stream()
                .map(PatientAppointment::getAppointmentId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAppointments(UUID patientId) {
        return existsByPatientId(patientId);
    }
}
