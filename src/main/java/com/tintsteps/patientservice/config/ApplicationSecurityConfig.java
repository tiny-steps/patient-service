package com.tintsteps.patientservice.config;

import com.tintsteps.patientservice.repository.PatientAddressRepository;
import com.tintsteps.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Security configuration for patient-related operations
 * Provides methods for checking ownership and access permissions
 */
@Slf4j
@Component("patientSecurity")
@RequiredArgsConstructor
public class ApplicationSecurityConfig {

    private final PatientRepository patientRepository;
    private final PatientAddressRepository patientAddressRepository;

    /**
     * Check if the authenticated user is the owner of the patient record
     */
    public boolean isPatientOwner(Authentication authentication, UUID patientId) {
        try {
            if (authentication == null || authentication.getName() == null) {
                log.warn("Authentication is null or has no name");
                return false;
            }

            UUID currentUserId = UUID.fromString(authentication.getName());

            return patientRepository.findById(patientId)
                    .map(patient -> patient.getUserId().equals(currentUserId))
                    .orElse(false);
        } catch (Exception e) {
            log.error("Error checking patient ownership: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the authenticated user is the owner of the user record
     */
    public boolean isUserOwner(Authentication authentication, UUID userId) {
        try {
            if (authentication == null || authentication.getName() == null) {
                log.warn("Authentication is null or has no name");
                return false;
            }

            UUID currentUserId = UUID.fromString(authentication.getName());
            return currentUserId.equals(userId);
        } catch (Exception e) {
            log.error("Error checking user ownership: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if the authenticated user owns any of the patient records in the list
     */
    public boolean isPatientOwnerInList(Authentication authentication, UUID patientId) {
        return isPatientOwner(authentication, patientId);
    }

    /**
     * Check if the authenticated user has admin role
     */
    public boolean isAdmin(Authentication authentication) {
        return authentication != null &&
               authentication.getAuthorities().stream()
                       .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Check if the authenticated user has doctor role
     */
    public boolean isDoctor(Authentication authentication) {
        return authentication != null &&
               authentication.getAuthorities().stream()
                       .anyMatch(authority -> authority.getAuthority().equals("ROLE_DOCTOR"));
    }

    /**
     * Check if the authenticated user has patient role
     */
    public boolean isPatient(Authentication authentication) {
        return authentication != null &&
               authentication.getAuthorities().stream()
                       .anyMatch(authority -> authority.getAuthority().equals("ROLE_PATIENT"));
    }

    public boolean getIsPatientAddressOwner(Authentication authentication, UUID patientId) {
        try {
            if (authentication == null || authentication.getName() == null) {
                log.warn("Authentication is null or has no name");
                return false;
            }

            UUID currentUserId = UUID.fromString(authentication.getName());

            return patientAddressRepository.findById(currentUserId)
                    .map(patient -> patient.getAddressId().equals(patientId))
                    .orElse(false);
        } catch (Exception e) {
            log.error("Error checking patient ownership: {}", e.getMessage());
            return false;
        }
    }
}
