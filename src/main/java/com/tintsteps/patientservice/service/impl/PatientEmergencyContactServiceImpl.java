package com.tintsteps.patientservice.service.impl;

import com.tintsteps.patientservice.dto.PatientEmergencyContactDto;
import com.tintsteps.patientservice.exception.PatientNotFoundException;
import com.tintsteps.patientservice.exception.PatientServiceException;
import com.tintsteps.patientservice.mapper.PatientEmergencyContactMapper;
import com.tintsteps.patientservice.model.Patient;
import com.tintsteps.patientservice.model.PatientEmergencyContact;
import com.tintsteps.patientservice.repository.PatientEmergencyContactRepository;
import com.tintsteps.patientservice.repository.PatientRepository;
import com.tintsteps.patientservice.service.PatientEmergencyContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class PatientEmergencyContactServiceImpl implements PatientEmergencyContactService {

    private final PatientEmergencyContactRepository patientEmergencyContactRepository;
    private final PatientRepository patientRepository;
    private final PatientEmergencyContactMapper patientEmergencyContactMapper = PatientEmergencyContactMapper.INSTANCE;

    @Override
    @Transactional
    @CacheEvict(value = { "patient-emergency-contacts", "patients" }, allEntries = true)
    public PatientEmergencyContactDto create(PatientEmergencyContactDto patientEmergencyContactDto) {
        log.info("Creating emergency contact for patient ID: {}", patientEmergencyContactDto.getPatientId());

        try {
            if (patientEmergencyContactDto.getPatientId() == null) {
                throw new IllegalArgumentException("Patient ID is required");
            }

            // Verify patient exists
            Patient patient = patientRepository.findById(patientEmergencyContactDto.getPatientId())
                    .orElseThrow(() -> new PatientNotFoundException(patientEmergencyContactDto.getPatientId()));

            PatientEmergencyContact contact = patientEmergencyContactMapper
                    .patientEmergencyContactDtoToPatientEmergencyContact(patientEmergencyContactDto);
            contact.setPatient(patient);

            PatientEmergencyContact savedContact = patientEmergencyContactRepository.save(contact);

            log.info("Emergency contact created successfully with ID: {}", savedContact.getId());
            return patientEmergencyContactMapper.patientEmergencyContactToPatientEmergencyContactDto(savedContact);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating emergency contact: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create emergency contact", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "patient-emergency-contacts", key = "#id")
    public PatientEmergencyContactDto findById(UUID id) {
        log.info("Finding emergency contact by ID: {}", id);

        PatientEmergencyContact contact = patientEmergencyContactRepository.findById(id)
                .orElseThrow(() -> new PatientServiceException("Emergency contact not found with id: " + id));

        return patientEmergencyContactMapper.patientEmergencyContactToPatientEmergencyContactDto(contact);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientEmergencyContactDto> findByPatientId(UUID patientId) {
        log.info("Finding emergency contacts by patient ID: {}", patientId);

        List<PatientEmergencyContact> contacts = patientEmergencyContactRepository.findByPatientId(patientId);
        return contacts.stream()
                .map(patientEmergencyContactMapper::patientEmergencyContactToPatientEmergencyContactDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientEmergencyContactDto> findAll(Pageable pageable) {
        log.info("Finding all emergency contacts with pagination");

        Page<PatientEmergencyContact> contacts = patientEmergencyContactRepository.findAll(pageable);
        return contacts.map(patientEmergencyContactMapper::patientEmergencyContactToPatientEmergencyContactDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "patient-emergency-contacts", "patients" }, key = "#id")
    public PatientEmergencyContactDto update(UUID id, PatientEmergencyContactDto patientEmergencyContactDto) {
        log.info("Updating emergency contact with ID: {}", id);

        try {
            PatientEmergencyContact existingContact = patientEmergencyContactRepository.findById(id)
                    .orElseThrow(() -> new PatientServiceException("Emergency contact not found with id: " + id));

            // Update fields
            if (patientEmergencyContactDto.getName() != null) {
                existingContact.setName(patientEmergencyContactDto.getName());
            }
            if (patientEmergencyContactDto.getRelationship() != null) {
                existingContact.setRelationship(patientEmergencyContactDto.getRelationship());
            }
            if (patientEmergencyContactDto.getPhone() != null) {
                existingContact.setPhone(patientEmergencyContactDto.getPhone());
            }

            PatientEmergencyContact updatedContact = patientEmergencyContactRepository.save(existingContact);
            return patientEmergencyContactMapper.patientEmergencyContactToPatientEmergencyContactDto(updatedContact);
        } catch (Exception e) {
            log.error("Error updating emergency contact: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to update emergency contact", e);
        }
    }

    @Override
    @Transactional
    public PatientEmergencyContactDto partialUpdate(UUID id, PatientEmergencyContactDto patientEmergencyContactDto) {
        return update(id, patientEmergencyContactDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "patient-emergency-contacts", "patients" }, key = "#id")
    public void delete(UUID id) {
        log.info("Deleting emergency contact with ID: {}", id);

        try {
            if (!patientEmergencyContactRepository.existsById(id)) {
                throw new PatientServiceException("Emergency contact not found with id: " + id);
            }

            patientEmergencyContactRepository.deleteById(id);
            log.info("Emergency contact deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting emergency contact: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to delete emergency contact", e);
        }
    }

    // Search Operations

    @Override
    @Transactional(readOnly = true)
    public Page<PatientEmergencyContactDto> findByRelationship(String relationship, Pageable pageable) {
        Page<PatientEmergencyContact> contacts = patientEmergencyContactRepository
                .findByRelationshipContainingIgnoreCase(relationship, pageable);
        return contacts.map(patientEmergencyContactMapper::patientEmergencyContactToPatientEmergencyContactDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientEmergencyContactDto> findByPatientId(UUID patientId, Pageable pageable) {
        Page<PatientEmergencyContact> contacts = patientEmergencyContactRepository.findByPatientId(patientId, pageable);
        return contacts.map(patientEmergencyContactMapper::patientEmergencyContactToPatientEmergencyContactDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientEmergencyContactDto> searchContacts(UUID patientId, String name, String relationship,
            String phone, Pageable pageable) {
        // For now, implement basic search - can be enhanced with Specifications
        Page<PatientEmergencyContact> contacts = patientEmergencyContactRepository.findAll(pageable);
        return contacts.map(patientEmergencyContactMapper::patientEmergencyContactToPatientEmergencyContactDto);
    }

    // Business Operations
    @Override
    @Transactional
    public PatientEmergencyContactDto addEmergencyContact(UUID patientId, String name, String relationship,
            String phone) {
        log.info("Adding emergency contact for patient ID: {}", patientId);

        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(patientId));

            PatientEmergencyContact contact = new PatientEmergencyContact();
            contact.setPatient(patient);
            contact.setName(name);
            contact.setRelationship(relationship);
            contact.setPhone(phone);

            PatientEmergencyContact savedContact = patientEmergencyContactRepository.save(contact);
            return patientEmergencyContactMapper.patientEmergencyContactToPatientEmergencyContactDto(savedContact);
        } catch (PatientNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding emergency contact: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to add emergency contact", e);
        }
    }

    @Override
    @Transactional
    public void removeEmergencyContact(UUID patientId, String phone) {
        log.info("Removing emergency contact for patient ID: {} with phone: {}", patientId, phone);

        List<PatientEmergencyContact> contacts = patientEmergencyContactRepository.findByPatientId(patientId);
        List<PatientEmergencyContact> contactsToDelete = contacts.stream()
                .filter(contact -> contact.getPhone().equals(phone))
                .collect(Collectors.toList());

        if (!contactsToDelete.isEmpty()) {
            patientEmergencyContactRepository.deleteAll(contactsToDelete);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEmergencyContact(UUID patientId, String phone) {
        return patientEmergencyContactRepository.existsByPatientIdAndPhone(patientId, phone);
    }

    @Override
    @Transactional
    public PatientEmergencyContactDto updateContactInfo(UUID id, String name, String relationship, String phone) {
        PatientEmergencyContactDto dto = new PatientEmergencyContactDto();
        dto.setName(name);
        dto.setRelationship(relationship);
        dto.setPhone(phone);
        return update(id, dto);
    }

    // Validation Operations
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return patientEmergencyContactRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPatientId(UUID patientId) {
        return patientEmergencyContactRepository.existsByPatientId(patientId);
    }

    // Statistics Operations
    @Override
    @Transactional(readOnly = true)
    public long countByPatientId(UUID patientId) {
        return patientEmergencyContactRepository.countByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return patientEmergencyContactRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getRelationshipStatistics() {
        return patientEmergencyContactRepository.getRelationshipStatistics();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctRelationships() {
        return patientEmergencyContactRepository.findDistinctRelationships();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findPatientsWithMultipleContacts() {
        return patientEmergencyContactRepository.findPatientsWithMultipleContacts();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findPatientsWithoutEmergencyContacts() {
        return patientEmergencyContactRepository.findPatientsWithoutEmergencyContacts();
    }

    // Bulk Operations
    @Override
    @Transactional
    public List<PatientEmergencyContactDto> createBatch(List<PatientEmergencyContactDto> patientEmergencyContactDtos) {
        log.info("Creating batch of {} emergency contacts", patientEmergencyContactDtos.size());

        try {
            List<PatientEmergencyContact> contacts = patientEmergencyContactDtos.stream()
                    .map(dto -> {
                        Patient patient = patientRepository.findById(dto.getPatientId())
                                .orElseThrow(() -> new PatientNotFoundException(dto.getPatientId()));
                        PatientEmergencyContact contact = patientEmergencyContactMapper
                                .patientEmergencyContactDtoToPatientEmergencyContact(dto);
                        contact.setPatient(patient);
                        return contact;
                    })
                    .collect(Collectors.toList());

            List<PatientEmergencyContact> savedContacts = patientEmergencyContactRepository.saveAll(contacts);
            return savedContacts.stream()
                    .map(patientEmergencyContactMapper::patientEmergencyContactToPatientEmergencyContactDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error creating batch emergency contacts: {}", e.getMessage(), e);
            throw new PatientServiceException("Failed to create batch emergency contacts", e);
        }
    }

    @Override
    @Transactional
    public void deleteByPatientId(UUID patientId) {
        log.info("Deleting all emergency contacts for patient ID: {}", patientId);
        patientEmergencyContactRepository.deleteByPatientId(patientId);
    }

    // Contact Management
    @Override
    @Transactional(readOnly = true)
    public int getEmergencyContactCount(UUID patientId) {
        return (int) countByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getEmergencyContactPhones(UUID patientId) {
        List<PatientEmergencyContact> contacts = patientEmergencyContactRepository.findByPatientId(patientId);
        return contacts.stream()
                .map(PatientEmergencyContact::getPhone)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEmergencyContacts(UUID patientId) {
        return existsByPatientId(patientId);
    }

}
