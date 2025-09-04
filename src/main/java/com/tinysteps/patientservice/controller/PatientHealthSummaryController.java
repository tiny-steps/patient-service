package com.tinysteps.patientservice.controller;

import com.tinysteps.patientservice.dto.*;
import com.tinysteps.patientservice.model.ResponseModel;
import com.tinysteps.patientservice.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/patient-health-summary")
@RequiredArgsConstructor
public class PatientHealthSummaryController {

    private final PatientService patientService;
    private final PatientAllergyService patientAllergyService;
    private final PatientMedicationService patientMedicationService;
    private final PatientEmergencyContactService patientEmergencyContactService;
    private final PatientInsuranceService patientInsuranceService;
    private final PatientMedicalHistoryService patientMedicalHistoryService;
    private final PatientAddressService patientAddressService;
    private final PatientAppointmentService patientAppointmentService;

    @GetMapping("/{patientId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientHealthSummaryDto>> getPatientHealthSummary(@PathVariable UUID patientId) {
        log.info("Getting comprehensive health summary for patient ID: {}", patientId);
        
        // Get basic patient info
        PatientDto patient = patientService.findById(patientId);
        
        // Get all related data
        List<PatientAllergyDto> allergies = patientAllergyService.findByPatientId(patientId);
        List<PatientMedicationDto> currentMedications = patientMedicationService.getCurrentMedicationsForPatient(patientId);
        List<PatientMedicationDto> allMedications = patientMedicationService.findByPatientId(patientId);
        List<PatientEmergencyContactDto> emergencyContacts = patientEmergencyContactService.findByPatientId(patientId);
        List<PatientInsuranceDto> insurance = patientInsuranceService.findByPatientId(patientId);
        List<PatientMedicalHistoryDto> medicalHistory = patientMedicalHistoryService.findByPatientId(patientId);
        List<PatientAddressDto> addresses = patientAddressService.findByPatientId(patientId);
        List<PatientAppointmentDto> appointments = patientAppointmentService.findByPatientId(patientId);
        
        // Build comprehensive summary
        PatientHealthSummaryDto summary = PatientHealthSummaryDto.builder()
                .patient(patient)
                .allergies(allergies)
                .currentMedications(currentMedications)
                .allMedications(allMedications)
                .emergencyContacts(emergencyContacts)
                .insurance(insurance)
                .medicalHistory(medicalHistory)
                .addresses(addresses)
                .appointments(appointments)
                .build();
        
        return ResponseEntity.ok(ResponseModel.success(summary, "Patient health summary retrieved successfully"));
    }

    @GetMapping("/{patientId}/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientDashboardDto>> getPatientDashboard(@PathVariable UUID patientId) {
        log.info("Getting dashboard data for patient ID: {}", patientId);
        
        // Get basic patient info
        PatientDto patient = patientService.findById(patientId);
        
        // Get key metrics
        List<String> criticalAllergies = patientAllergyService.getCriticalAllergies(patientId);
        List<String> activeMedications = patientMedicationService.getActiveMedicationNames(patientId);
        List<PatientMedicationDto> expiringMedications = patientMedicationService.getExpiringMedications(patientId, 30);
        List<PatientEmergencyContactDto> emergencyContacts = patientEmergencyContactService.findByPatientId(patientId);
        List<PatientMedicalHistoryDto> recentHistory = patientMedicalHistoryService.getRecentMedicalHistory(patientId, 90);
        
        // Calculate profile completeness
        int profileCompleteness = patientService.calculateProfileCompleteness(patientId);
        
        // Build dashboard
        PatientDashboardDto dashboard = PatientDashboardDto.builder()
                .patient(patient)
                .profileCompleteness(profileCompleteness)
                .criticalAllergies(criticalAllergies)
                .activeMedications(activeMedications)
                .expiringMedications(expiringMedications)
                .emergencyContacts(emergencyContacts)
                .recentMedicalHistory(recentHistory)
                .hasInsurance(patientInsuranceService.hasInsurance(patientId))
                .hasEmergencyContacts(!emergencyContacts.isEmpty())
                .hasCriticalAllergies(!criticalAllergies.isEmpty())
                .build();
        
        return ResponseEntity.ok(ResponseModel.success(dashboard, "Patient dashboard retrieved successfully"));
    }

    @GetMapping("/{patientId}/safety-alerts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientSafetyAlertsDto>> getPatientSafetyAlerts(@PathVariable UUID patientId) {
        log.info("Getting safety alerts for patient ID: {}", patientId);
        
        // Get critical allergies
        List<String> criticalAllergies = patientAllergyService.getCriticalAllergies(patientId);
        
        // Get active medications
        List<String> activeMedications = patientMedicationService.getActiveMedicationNames(patientId);
        
        // Get expiring medications
        List<PatientMedicationDto> expiringMedications = patientMedicationService.getExpiringMedications(patientId, 30);
        
        // Get chronic conditions
        List<String> chronicConditions = patientMedicalHistoryService.getChronicConditions(patientId);
        
        // Check for missing emergency contacts
        boolean hasEmergencyContacts = patientEmergencyContactService.hasEmergencyContacts(patientId);
        
        // Check for missing insurance
        boolean hasInsurance = patientInsuranceService.hasInsurance(patientId);
        
        // Build safety alerts
        PatientSafetyAlertsDto alerts = PatientSafetyAlertsDto.builder()
                .criticalAllergies(criticalAllergies)
                .activeMedications(activeMedications)
                .expiringMedications(expiringMedications)
                .chronicConditions(chronicConditions)
                .missingEmergencyContacts(!hasEmergencyContacts)
                .missingInsurance(!hasInsurance)
                .hasCriticalAllergies(!criticalAllergies.isEmpty())
                .hasExpiringMedications(!expiringMedications.isEmpty())
                .hasChronicConditions(!chronicConditions.isEmpty())
                .build();
        
        return ResponseEntity.ok(ResponseModel.success(alerts, "Patient safety alerts retrieved successfully"));
    }

    @GetMapping("/{patientId}/medication-safety")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<MedicationSafetyDto>> checkMedicationSafety(
            @PathVariable UUID patientId,
            @RequestParam(required = false) String newMedication) {
        log.info("Checking medication safety for patient ID: {}", patientId);
        
        // Get current medications
        List<String> currentMedications = patientMedicationService.getActiveMedicationNames(patientId);
        
        // Get allergies
        List<String> allergens = patientAllergyService.getAllergens(patientId);
        
        // Check for conflicts if new medication is provided
        boolean hasConflicts = false;
        boolean hasAllergyConflict = false;
        List<String> potentialInteractions = List.of();
        
        if (newMedication != null) {
            hasConflicts = patientMedicationService.hasMedicationConflicts(patientId, newMedication);
            hasAllergyConflict = allergens.stream()
                    .anyMatch(allergen -> allergen.toLowerCase().contains(newMedication.toLowerCase()));
            potentialInteractions = patientMedicationService.getPotentialInteractions(patientId, newMedication);
        }
        
        MedicationSafetyDto safety = MedicationSafetyDto.builder()
                .currentMedications(currentMedications)
                .allergens(allergens)
                .newMedication(newMedication)
                .hasConflicts(hasConflicts)
                .hasAllergyConflict(hasAllergyConflict)
                .potentialInteractions(potentialInteractions)
                .isSafeToAdd(!hasConflicts && !hasAllergyConflict)
                .build();
        
        return ResponseEntity.ok(ResponseModel.success(safety, "Medication safety check completed"));
    }

    @GetMapping("/{patientId}/timeline")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientTimelineDto>> getPatientTimeline(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "365") int daysBack) {
        log.info("Getting timeline for patient ID: {} for last {} days", patientId, daysBack);
        
        // Get recent medical history
        List<PatientMedicalHistoryDto> recentHistory = patientMedicalHistoryService.getRecentMedicalHistory(patientId, daysBack);
        
        // Get medication history
        List<PatientMedicationDto> medicationHistory = patientMedicationService.getMedicationHistory(patientId);
        
        // Get appointments
        List<PatientAppointmentDto> appointments = patientAppointmentService.findByPatientId(patientId);
        
        PatientTimelineDto timeline = PatientTimelineDto.builder()
                .patientId(patientId)
                .daysBack(daysBack)
                .medicalHistory(recentHistory)
                .medicationHistory(medicationHistory)
                .appointments(appointments)
                .build();
        
        return ResponseEntity.ok(ResponseModel.success(timeline, "Patient timeline retrieved successfully"));
    }

    @GetMapping("/{patientId}/care-plan")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or @patientSecurity.isPatientOwner(authentication, #patientId)")
    public ResponseEntity<ResponseModel<PatientCarePlanDto>> getPatientCarePlan(@PathVariable UUID patientId) {
        log.info("Getting care plan for patient ID: {}", patientId);
        
        // Get patient basic info
        PatientDto patient = patientService.findById(patientId);
        
        // Get current medications
        List<PatientMedicationDto> currentMedications = patientMedicationService.getCurrentMedicationsForPatient(patientId);
        
        // Get chronic conditions
        List<String> chronicConditions = patientMedicalHistoryService.getChronicConditions(patientId);
        
        // Get critical allergies
        List<String> criticalAllergies = patientAllergyService.getCriticalAllergies(patientId);
        
        // Get upcoming appointments
        List<PatientAppointmentDto> appointments = patientAppointmentService.findByPatientId(patientId);
        
        PatientCarePlanDto carePlan = PatientCarePlanDto.builder()
                .patient(patient)
                .currentMedications(currentMedications)
                .chronicConditions(chronicConditions)
                .criticalAllergies(criticalAllergies)
                .upcomingAppointments(appointments)
                .build();
        
        return ResponseEntity.ok(ResponseModel.success(carePlan, "Patient care plan retrieved successfully"));
    }

    @GetMapping("/{patientId}/risk-assessment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<ResponseModel<PatientRiskAssessmentDto>> getPatientRiskAssessment(@PathVariable UUID patientId) {
        log.info("Getting risk assessment for patient ID: {}", patientId);
        
        // Get patient info
        PatientDto patient = patientService.findById(patientId);
        
        // Calculate risk factors
        boolean hasCriticalAllergies = patientAllergyService.hasCriticalAllergies(patientId);
        boolean hasChronicConditions = !patientMedicalHistoryService.getChronicConditions(patientId).isEmpty();
        boolean hasMultipleMedications = patientMedicationService.getActiveMedicationCount(patientId) > 3;
        boolean missingEmergencyContacts = !patientEmergencyContactService.hasEmergencyContacts(patientId);
        boolean missingInsurance = !patientInsuranceService.hasInsurance(patientId);
        
        // Calculate risk score (simple scoring system)
        int riskScore = 0;
        if (hasCriticalAllergies) riskScore += 20;
        if (hasChronicConditions) riskScore += 15;
        if (hasMultipleMedications) riskScore += 10;
        if (missingEmergencyContacts) riskScore += 10;
        if (missingInsurance) riskScore += 5;
        
        String riskLevel = riskScore >= 40 ? "HIGH" : riskScore >= 20 ? "MEDIUM" : "LOW";
        
        PatientRiskAssessmentDto riskAssessment = PatientRiskAssessmentDto.builder()
                .patientId(patientId)
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .hasCriticalAllergies(hasCriticalAllergies)
                .hasChronicConditions(hasChronicConditions)
                .hasMultipleMedications(hasMultipleMedications)
                .missingEmergencyContacts(missingEmergencyContacts)
                .missingInsurance(missingInsurance)
                .build();
        
        return ResponseEntity.ok(ResponseModel.success(riskAssessment, "Patient risk assessment completed"));
    }
}
