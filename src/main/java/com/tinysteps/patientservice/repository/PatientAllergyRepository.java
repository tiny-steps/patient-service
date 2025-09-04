package com.tinysteps.patientservice.repository;

import com.tinysteps.patientservice.model.PatientAllergy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PatientAllergyRepository extends JpaRepository<PatientAllergy, UUID> {

    // Find by patient ID
    List<PatientAllergy> findByPatientId(UUID patientId);
    Page<PatientAllergy> findByPatientId(UUID patientId, Pageable pageable);

    // Find by allergen
    List<PatientAllergy> findByAllergenContainingIgnoreCase(String allergen);
    Page<PatientAllergy> findByAllergenContainingIgnoreCase(String allergen, Pageable pageable);

    // Find by reaction
    List<PatientAllergy> findByReactionContainingIgnoreCase(String reaction);
    Page<PatientAllergy> findByReactionContainingIgnoreCase(String reaction, Pageable pageable);

    // Find by patient and allergen
    List<PatientAllergy> findByPatientIdAndAllergenContainingIgnoreCase(UUID patientId, String allergen);

    // Find by recorded date range
    List<PatientAllergy> findByRecordedAtBetween(Instant startDate, Instant endDate);
    Page<PatientAllergy> findByRecordedAtBetween(Instant startDate, Instant endDate, Pageable pageable);

    // Validation methods
    boolean existsByPatientId(UUID patientId);
    boolean existsByPatientIdAndAllergen(UUID patientId, String allergen);

    // Count methods
    long countByPatientId(UUID patientId);
    long countByAllergenContainingIgnoreCase(String allergen);

    // Delete methods
    void deleteByPatientId(UUID patientId);

    // Statistics methods
    @Query("SELECT pa.allergen, COUNT(pa) FROM PatientAllergy pa GROUP BY pa.allergen ORDER BY COUNT(pa) DESC")
    List<Object[]> getAllergenStatistics();

    @Query("SELECT DISTINCT pa.allergen FROM PatientAllergy pa WHERE pa.allergen IS NOT NULL ORDER BY pa.allergen")
    List<String> findDistinctAllergens();

    @Query("SELECT DISTINCT pa.reaction FROM PatientAllergy pa WHERE pa.reaction IS NOT NULL ORDER BY pa.reaction")
    List<String> findDistinctReactions();
}
