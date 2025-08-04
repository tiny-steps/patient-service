package com.tintsteps.patientservice.repository;

import com.tintsteps.patientservice.model.PatientAppointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientAppointmentRepository extends JpaRepository<PatientAppointment, UUID> {

    // Find by patient ID
    List<PatientAppointment> findByPatientId(UUID patientId);
    Page<PatientAppointment> findByPatientId(UUID patientId, Pageable pageable);

    // Find by appointment ID
    List<PatientAppointment> findByAppointmentId(UUID appointmentId);
    Page<PatientAppointment> findByAppointmentId(UUID appointmentId, Pageable pageable);

    // Find by patient and appointment
    Optional<PatientAppointment> findByPatientIdAndAppointmentId(UUID patientId, UUID appointmentId);

    // Validation methods
    boolean existsByPatientId(UUID patientId);
    boolean existsByAppointmentId(UUID appointmentId);
    boolean existsByPatientIdAndAppointmentId(UUID patientId, UUID appointmentId);

    // Count methods
    long countByPatientId(UUID patientId);
    long countByAppointmentId(UUID appointmentId);

    // Delete methods
    void deleteByPatientId(UUID patientId);
    void deleteByAppointmentId(UUID appointmentId);
    void deleteByPatientIdAndAppointmentId(UUID patientId, UUID appointmentId);

    // Statistics methods
    @Query("SELECT COUNT(DISTINCT pa.appointmentId) FROM PatientAppointment pa")
    long countDistinctAppointments();

    @Query("SELECT COUNT(DISTINCT pa.patient.id) FROM PatientAppointment pa")
    long countDistinctPatients();

    @Query("SELECT pa.patient.id, COUNT(pa) FROM PatientAppointment pa GROUP BY pa.patient.id ORDER BY COUNT(pa) DESC")
    List<Object[]> getPatientAppointmentStatistics();

    // Find patients with multiple appointments
    @Query("SELECT pa.patient.id FROM PatientAppointment pa GROUP BY pa.patient.id HAVING COUNT(pa) > 1")
    List<UUID> findPatientsWithMultipleAppointments();

    // Find appointments linked to multiple patients (should not happen in normal cases)
    @Query("SELECT pa.appointmentId FROM PatientAppointment pa GROUP BY pa.appointmentId HAVING COUNT(pa) > 1")
    List<UUID> findAppointmentsWithMultiplePatients();
}
