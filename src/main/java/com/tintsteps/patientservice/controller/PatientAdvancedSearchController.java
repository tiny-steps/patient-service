package com.tintsteps.patientservice.controller;

import com.tintsteps.patientservice.dto.PatientDto;
import com.tintsteps.patientservice.model.Gender;
import com.tintsteps.patientservice.model.ResponseModel;
import com.tintsteps.patientservice.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/patient-advanced-search")
@RequiredArgsConstructor
public class PatientAdvancedSearchController {

    private final PatientService patientService;
    private final PatientAllergyService patientAllergyService;
    private final PatientMedicationService patientMedicationService;
    private final PatientMedicalHistoryService patientMedicalHistoryService;
    private final PatientInsuranceService patientInsuranceService;

    @GetMapping("/by-medical-condition")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchPatientsByMedicalCondition(
            @RequestParam String condition) {
        log.info("Searching patients by medical condition: {}", condition);
        
        // Find patients with the specific medical condition
        List<UUID> patientIds = patientMedicalHistoryService.findByCondition(condition)
                .stream()
                .map(history -> history.getPatientId())
                .distinct()
                .collect(Collectors.toList());
        
        List<PatientDto> patients = patientIds.stream()
                .map(patientService::findById)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseModel.success(patients, 
                "Patients with medical condition '" + condition + "' retrieved successfully"));
    }

    @GetMapping("/by-medication")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchPatientsByMedication(
            @RequestParam String medicationName) {
        log.info("Searching patients by medication: {}", medicationName);
        
        // Find patients taking the specific medication
        List<UUID> patientIds = patientMedicationService.findByMedicationName(medicationName)
                .stream()
                .map(medication -> medication.getPatientId())
                .distinct()
                .collect(Collectors.toList());
        
        List<PatientDto> patients = patientIds.stream()
                .map(patientService::findById)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseModel.success(patients, 
                "Patients taking medication '" + medicationName + "' retrieved successfully"));
    }

    @GetMapping("/by-allergy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchPatientsByAllergy(
            @RequestParam String allergen) {
        log.info("Searching patients by allergy: {}", allergen);
        
        // Find patients with the specific allergy
        List<UUID> patientIds = patientAllergyService.findByAllergen(allergen)
                .stream()
                .map(allergy -> allergy.getPatientId())
                .distinct()
                .collect(Collectors.toList());
        
        List<PatientDto> patients = patientIds.stream()
                .map(patientService::findById)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseModel.success(patients, 
                "Patients with allergy '" + allergen + "' retrieved successfully"));
    }

    @GetMapping("/by-age-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> searchPatientsByAgeRange(
            @RequestParam Integer minAge,
            @RequestParam Integer maxAge,
            Pageable pageable) {
        log.info("Searching patients by age range: {} to {}", minAge, maxAge);
        
        Page<PatientDto> patients = patientService.findByAgeRange(minAge, maxAge, pageable);
        
        return ResponseEntity.ok(ResponseModel.success(patients, 
                "Patients in age range " + minAge + "-" + maxAge + " retrieved successfully"));
    }

    @GetMapping("/by-gender")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> searchPatientsByGender(
            @RequestParam Gender gender,
            Pageable pageable) {
        log.info("Searching patients by gender: {}", gender);
        
        Page<PatientDto> patients = patientService.findByGender(gender, pageable);
        
        return ResponseEntity.ok(ResponseModel.success(patients, 
                "Patients with gender '" + gender + "' retrieved successfully"));
    }

    @GetMapping("/by-blood-group")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<Page<PatientDto>>> searchPatientsByBloodGroup(
            @RequestParam String bloodGroup,
            Pageable pageable) {
        log.info("Searching patients by blood group: {}", bloodGroup);
        
        Page<PatientDto> patients = patientService.findByBloodGroup(bloodGroup, pageable);
        
        return ResponseEntity.ok(ResponseModel.success(patients, 
                "Patients with blood group '" + bloodGroup + "' retrieved successfully"));
    }

    @GetMapping("/by-insurance-provider")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchPatientsByInsuranceProvider(
            @RequestParam String provider) {
        log.info("Searching patients by insurance provider: {}", provider);
        
        // Find patients with the specific insurance provider
        List<UUID> patientIds = patientInsuranceService.findByProvider(provider)
                .stream()
                .map(insurance -> insurance.getPatientId())
                .distinct()
                .collect(Collectors.toList());
        
        List<PatientDto> patients = patientIds.stream()
                .map(patientService::findById)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseModel.success(patients, 
                "Patients with insurance provider '" + provider + "' retrieved successfully"));
    }

    @GetMapping("/critical-allergies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchPatientsWithCriticalAllergies() {
        log.info("Searching patients with critical allergies");
        
        // Find all patients and filter those with critical allergies
        List<PatientDto> allPatients = patientService.findAll();
        List<PatientDto> patientsWithCriticalAllergies = allPatients.stream()
                .filter(patient -> patientAllergyService.hasCriticalAllergies(patient.getId()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseModel.success(patientsWithCriticalAllergies, 
                "Patients with critical allergies retrieved successfully"));
    }

    @GetMapping("/without-insurance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchPatientsWithoutInsurance() {
        log.info("Searching patients without insurance");
        
        List<UUID> patientIdsWithoutInsurance = patientInsuranceService.findPatientsWithoutInsurance();
        List<PatientDto> patients = patientIdsWithoutInsurance.stream()
                .map(patientService::findById)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseModel.success(patients, 
                "Patients without insurance retrieved successfully"));
    }

    @GetMapping("/without-emergency-contacts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchPatientsWithoutEmergencyContacts() {
        log.info("Searching patients without emergency contacts");
        
        // Find all patients and filter those without emergency contacts
        List<PatientDto> allPatients = patientService.findAll();
        List<PatientDto> patientsWithoutContacts = allPatients.stream()
                .filter(patient -> !patientService.hasEmergencyContacts(patient.getId()))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseModel.success(patientsWithoutContacts, 
                "Patients without emergency contacts retrieved successfully"));
    }

    @GetMapping("/multiple-medications")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchPatientsWithMultipleMedications(
            @RequestParam(defaultValue = "3") int minimumMedications) {
        log.info("Searching patients with {} or more medications", minimumMedications);
        
        // Find all patients and filter those with multiple medications
        List<PatientDto> allPatients = patientService.findAll();
        List<PatientDto> patientsWithMultipleMedications = allPatients.stream()
                .filter(patient -> patientMedicationService.getActiveMedicationCount(patient.getId()) >= minimumMedications)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseModel.success(patientsWithMultipleMedications, 
                "Patients with " + minimumMedications + "+ medications retrieved successfully"));
    }

    @GetMapping("/chronic-conditions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchPatientsWithChronicConditions() {
        log.info("Searching patients with chronic conditions");
        
        // Find all patients and filter those with chronic conditions
        List<PatientDto> allPatients = patientService.findAll();
        List<PatientDto> patientsWithChronicConditions = allPatients.stream()
                .filter(patient -> !patientMedicalHistoryService.getChronicConditions(patient.getId()).isEmpty())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseModel.success(patientsWithChronicConditions, 
                "Patients with chronic conditions retrieved successfully"));
    }

    @GetMapping("/high-risk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchHighRiskPatients() {
        log.info("Searching high-risk patients");
        
        // Find all patients and filter high-risk ones
        List<PatientDto> allPatients = patientService.findAll();
        List<PatientDto> highRiskPatients = allPatients.stream()
                .filter(patient -> {
                    boolean hasCriticalAllergies = patientAllergyService.hasCriticalAllergies(patient.getId());
                    boolean hasChronicConditions = !patientMedicalHistoryService.getChronicConditions(patient.getId()).isEmpty();
                    boolean hasMultipleMedications = patientMedicationService.getActiveMedicationCount(patient.getId()) > 3;
                    
                    // Consider high-risk if patient has 2 or more risk factors
                    int riskFactors = 0;
                    if (hasCriticalAllergies) riskFactors++;
                    if (hasChronicConditions) riskFactors++;
                    if (hasMultipleMedications) riskFactors++;
                    
                    return riskFactors >= 2;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ResponseModel.success(highRiskPatients, 
                "High-risk patients retrieved successfully"));
    }

    @GetMapping("/multi-criteria")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<List<PatientDto>>> searchPatientsByMultipleCriteria(
            @RequestParam(required = false) String medicalCondition,
            @RequestParam(required = false) String medication,
            @RequestParam(required = false) String allergen,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String bloodGroup,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge) {
        log.info("Searching patients by multiple criteria");
        
        List<PatientDto> results = patientService.findAll();
        
        // Apply filters based on provided criteria
        if (medicalCondition != null) {
            List<UUID> patientIdsWithCondition = patientMedicalHistoryService.findByCondition(medicalCondition)
                    .stream()
                    .map(history -> history.getPatientId())
                    .collect(Collectors.toList());
            results = results.stream()
                    .filter(patient -> patientIdsWithCondition.contains(patient.getId()))
                    .collect(Collectors.toList());
        }
        
        if (medication != null) {
            List<UUID> patientIdsWithMedication = patientMedicationService.findByMedicationName(medication)
                    .stream()
                    .map(med -> med.getPatientId())
                    .collect(Collectors.toList());
            results = results.stream()
                    .filter(patient -> patientIdsWithMedication.contains(patient.getId()))
                    .collect(Collectors.toList());
        }
        
        if (allergen != null) {
            List<UUID> patientIdsWithAllergy = patientAllergyService.findByAllergen(allergen)
                    .stream()
                    .map(allergy -> allergy.getPatientId())
                    .collect(Collectors.toList());
            results = results.stream()
                    .filter(patient -> patientIdsWithAllergy.contains(patient.getId()))
                    .collect(Collectors.toList());
        }
        
        if (gender != null) {
            results = results.stream()
                    .filter(patient -> gender.equals(patient.getGender()))
                    .collect(Collectors.toList());
        }
        
        if (bloodGroup != null) {
            results = results.stream()
                    .filter(patient -> bloodGroup.equals(patient.getBloodGroup()))
                    .collect(Collectors.toList());
        }
        
        // Age filtering would require additional logic to calculate age from date of birth
        
        return ResponseEntity.ok(ResponseModel.success(results, 
                "Patients matching multiple criteria retrieved successfully"));
    }
}
