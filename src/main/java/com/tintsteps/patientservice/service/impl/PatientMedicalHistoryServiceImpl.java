package com.tintsteps.patientservice.service.impl;

import com.tintsteps.patientservice.dto.PatientMedicalHistoryDto;
import com.tintsteps.patientservice.exception.PatientNotFoundException;
import com.tintsteps.patientservice.exception.PatientServiceException;
import com.tintsteps.patientservice.mapper.PatientMedicalHistoryMapper;
import com.tintsteps.patientservice.model.Patient;
import com.tintsteps.patientservice.model.PatientMedicalHistory;
import com.tintsteps.patientservice.repository.PatientMedicalHistoryRepository;
import com.tintsteps.patientservice.repository.PatientRepository;
import com.tintsteps.patientservice.service.PatientMedicalHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientMedicalHistoryServiceImpl implements PatientMedicalHistoryService {

    private final PatientMedicalHistoryRepository patientMedicalHistoryRepository;
    private final PatientRepository patientRepository;
    private final PatientMedicalHistoryMapper patientMedicalHistoryMapper = PatientMedicalHistoryMapper.INSTANCE;

    @Override
    @Transactional
    public PatientMedicalHistoryDto create(PatientMedicalHistoryDto patientMedicalHistoryDto) {
        log.info("Creating medical history for patient ID: {}", patientMedicalHistoryDto.getPatientId());
        
        try {
            if (patientMedicalHistoryDto.getPatientId() == null) {
                throw new IllegalArgumentException("Patient ID is required");
            }
            
            // Verify patient exists
            Patient patient = patientRepository.findById(patientMedicalHistoryDto.getPatientId())
                    .orElseThrow(() -> new PatientNotFoundException(patientMedicalHistoryDto.getPatientId()));
            
            PatientMedicalHistory history = patientMedicalHistoryMapper.patientMedicalHistoryDtoToPatientMedicalHistory(patientMedicalHistoryDto);
            history.setPatient(patient);
            
            // Set recorded timestamp if not provided
            if (history.getRecordedAt() == null) {
                history.setRecordedAt(Timestamp.valueOf(LocalDateTime.now()));
            }
            
            PatientMedicalHistory savedHistory = patientMedicalHistoryRepository.save(history);
            
            log.info("Medical history created successfully with ID: {}", savedHistory.getId());
            return patientMedicalHistoryMapper.patientMedicalHistoryToPatientMedicalHistoryDto(savedHistory);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating medical history: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create medical history", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PatientMedicalHistoryDto findById(UUID id) {
        log.info("Finding medical history by ID: {}", id);
        
        PatientMedicalHistory history = patientMedicalHistoryRepository.findById(id)
                .orElseThrow(() -> new PatientServiceException("Medical history not found with id: " + id));
        
        return patientMedicalHistoryMapper.patientMedicalHistoryToPatientMedicalHistoryDto(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicalHistoryDto> findByPatientId(UUID patientId) {
        log.info("Finding medical history by patient ID: {}", patientId);
        
        List<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByPatientId(patientId);
        return histories.stream()
                .map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicalHistoryDto> findAll(Pageable pageable) {
        log.info("Finding all medical history with pagination");
        
        Page<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findAll(pageable);
        return histories.map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto);
    }

    @Override
    @Transactional
    public PatientMedicalHistoryDto update(UUID id, PatientMedicalHistoryDto patientMedicalHistoryDto) {
        log.info("Updating medical history with ID: {}", id);
        
        try {
            PatientMedicalHistory existingHistory = patientMedicalHistoryRepository.findById(id)
                    .orElseThrow(() -> new PatientServiceException("Medical history not found with id: " + id));
            
            // Update fields
            if (patientMedicalHistoryDto.getCondition() != null) {
                existingHistory.setCondition(patientMedicalHistoryDto.getCondition());
            }
            if (patientMedicalHistoryDto.getNotes() != null) {
                existingHistory.setNotes(patientMedicalHistoryDto.getNotes());
            }
            if (patientMedicalHistoryDto.getRecordedAt() != null) {
                existingHistory.setRecordedAt(patientMedicalHistoryDto.getRecordedAt());
            }
            
            PatientMedicalHistory updatedHistory = patientMedicalHistoryRepository.save(existingHistory);
            return patientMedicalHistoryMapper.patientMedicalHistoryToPatientMedicalHistoryDto(updatedHistory);
        } catch (Exception e) {
            log.error("Error updating medical history: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to update medical history", e);
        }
    }

    @Override
    @Transactional
    public PatientMedicalHistoryDto partialUpdate(UUID id, PatientMedicalHistoryDto patientMedicalHistoryDto) {
        return update(id, patientMedicalHistoryDto);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting medical history with ID: {}", id);
        
        try {
            if (!patientMedicalHistoryRepository.existsById(id)) {
                throw new PatientServiceException("Medical history not found with id: " + id);
            }
            
            patientMedicalHistoryRepository.deleteById(id);
            log.info("Medical history deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting medical history: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete medical history", e);
        }
    }

    // Search Operations
    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicalHistoryDto> findByCondition(String condition) {
        List<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByConditionContainingIgnoreCase(condition);
        return histories.stream()
                .map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicalHistoryDto> findByCondition(String condition, Pageable pageable) {
        Page<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByConditionContainingIgnoreCase(condition, pageable);
        return histories.map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicalHistoryDto> findByNotes(String notes) {
        List<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByNotesContainingIgnoreCase(notes);
        return histories.stream()
                .map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicalHistoryDto> findByNotes(String notes, Pageable pageable) {
        Page<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByNotesContainingIgnoreCase(notes, pageable);
        return histories.map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicalHistoryDto> findByPatientId(UUID patientId, Pageable pageable) {
        Page<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByPatientId(patientId, pageable);
        return histories.map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicalHistoryDto> findByPatientAndCondition(UUID patientId, String condition) {
        List<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByPatientIdAndConditionContainingIgnoreCase(patientId, condition);
        return histories.stream()
                .map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicalHistoryDto> findByDateRange(Timestamp startDate, Timestamp endDate) {
        List<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByRecordedAtBetween(startDate, endDate);
        return histories.stream()
                .map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicalHistoryDto> findByDateRange(Timestamp startDate, Timestamp endDate, Pageable pageable) {
        Page<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByRecordedAtBetween(startDate, endDate, pageable);
        return histories.map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientMedicalHistoryDto> searchMedicalHistory(UUID patientId, String condition, String notes, 
                                                              Timestamp startDate, Timestamp endDate, Pageable pageable) {
        // For now, implement basic search - can be enhanced with Specifications
        Page<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findAll(pageable);
        return histories.map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto);
    }

    // Business Operations
    @Override
    @Transactional
    public PatientMedicalHistoryDto addMedicalHistory(UUID patientId, String condition, String notes) {
        log.info("Adding medical history for patient ID: {}", patientId);
        
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(patientId));
            
            PatientMedicalHistory history = new PatientMedicalHistory();
            history.setPatient(patient);
            history.setCondition(condition);
            history.setNotes(notes);
            history.setRecordedAt(Timestamp.valueOf(LocalDateTime.now()));
            
            PatientMedicalHistory savedHistory = patientMedicalHistoryRepository.save(history);
            return patientMedicalHistoryMapper.patientMedicalHistoryToPatientMedicalHistoryDto(savedHistory);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding medical history: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to add medical history", e);
        }
    }

    @Override
    @Transactional
    public void removeMedicalHistory(UUID patientId, String condition) {
        log.info("Removing medical history for patient ID: {} with condition: {}", patientId, condition);
        
        List<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByPatientId(patientId);
        List<PatientMedicalHistory> historiesToDelete = histories.stream()
                .filter(history -> history.getCondition().equalsIgnoreCase(condition))
                .collect(Collectors.toList());
        
        if (!historiesToDelete.isEmpty()) {
            patientMedicalHistoryRepository.deleteAll(historiesToDelete);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicalHistoryDto> getMedicalHistoryForPatient(UUID patientId) {
        return findByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMedicalHistory(UUID patientId, String condition) {
        return patientMedicalHistoryRepository.existsByPatientIdAndCondition(patientId, condition);
    }

    @Override
    @Transactional
    public PatientMedicalHistoryDto updateMedicalHistory(UUID id, String condition, String notes) {
        PatientMedicalHistoryDto dto = new PatientMedicalHistoryDto();
        dto.setCondition(condition);
        dto.setNotes(notes);
        return update(id, dto);
    }

    // Validation Operations
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return patientMedicalHistoryRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPatientId(UUID patientId) {
        return patientMedicalHistoryRepository.existsByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPatientIdAndCondition(UUID patientId, String condition) {
        return patientMedicalHistoryRepository.existsByPatientIdAndCondition(patientId, condition);
    }

    // Statistics Operations
    @Override
    @Transactional(readOnly = true)
    public long countByPatientId(UUID patientId) {
        return patientMedicalHistoryRepository.countByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCondition(String condition) {
        return patientMedicalHistoryRepository.countByConditionContainingIgnoreCase(condition);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return patientMedicalHistoryRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getConditionStatistics() {
        return patientMedicalHistoryRepository.getConditionStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctConditions() {
        return patientMedicalHistoryRepository.findDistinctConditions();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findPatientsWithMultipleHistoryRecords() {
        return patientMedicalHistoryRepository.findPatientsWithMultipleHistoryRecords();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findPatientsWithoutMedicalHistory() {
        return patientMedicalHistoryRepository.findPatientsWithoutMedicalHistory();
    }

    // Bulk Operations
    @Override
    @Transactional
    public List<PatientMedicalHistoryDto> createBatch(List<PatientMedicalHistoryDto> patientMedicalHistoryDtos) {
        log.info("Creating batch of {} medical histories", patientMedicalHistoryDtos.size());

        try {
            List<PatientMedicalHistory> histories = patientMedicalHistoryDtos.stream()
                    .map(dto -> {
                        Patient patient = patientRepository.findById(dto.getPatientId())
                                .orElseThrow(() -> new PatientNotFoundException(dto.getPatientId()));
                        PatientMedicalHistory history = patientMedicalHistoryMapper.patientMedicalHistoryDtoToPatientMedicalHistory(dto);
                        history.setPatient(patient);
                        if (history.getRecordedAt() == null) {
                            history.setRecordedAt(Timestamp.valueOf(LocalDateTime.now()));
                        }
                        return history;
                    })
                    .collect(Collectors.toList());

            List<PatientMedicalHistory> savedHistories = patientMedicalHistoryRepository.saveAll(histories);
            return savedHistories.stream()
                    .map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error creating batch medical histories: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create batch medical histories", e);
        }
    }

    @Override
    @Transactional
    public void deleteByPatientId(UUID patientId) {
        log.info("Deleting all medical histories for patient ID: {}", patientId);
        patientMedicalHistoryRepository.deleteByPatientId(patientId);
    }

    @Override
    @Transactional
    public void deleteBatch(List<UUID> ids) {
        log.info("Deleting batch of {} medical histories", ids.size());
        patientMedicalHistoryRepository.deleteAllById(ids);
    }

    // Medical History Management
    @Override
    @Transactional(readOnly = true)
    public int getMedicalHistoryCount(UUID patientId) {
        return (int) countByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMultipleMedicalHistoryRecords(UUID patientId) {
        return countByPatientId(patientId) > 1;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getMedicalConditions(UUID patientId) {
        List<PatientMedicalHistory> histories = patientMedicalHistoryRepository.findByPatientId(patientId);
        return histories.stream()
                .map(PatientMedicalHistory::getCondition)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMedicalHistory(UUID patientId) {
        return existsByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMedicalHistoryDto> getRecentMedicalHistory(UUID patientId, int daysBack) {
        Timestamp sinceDate = Timestamp.valueOf(LocalDateTime.now().minusDays(daysBack));
        List<PatientMedicalHistory> recentHistories = patientMedicalHistoryRepository.findRecentMedicalHistory(sinceDate);

        return recentHistories.stream()
                .filter(history -> history.getPatient().getId().equals(patientId))
                .map(patientMedicalHistoryMapper::patientMedicalHistoryToPatientMedicalHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getChronicConditions(UUID patientId) {
        // For now, return all conditions - could be enhanced with chronic condition logic
        return getMedicalConditions(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasChronicCondition(UUID patientId, String condition) {
        return hasMedicalHistory(patientId, condition);
    }
}
