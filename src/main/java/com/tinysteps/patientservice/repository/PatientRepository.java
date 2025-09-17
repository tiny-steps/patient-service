package com.tinysteps.patientservice.repository;

import com.tinysteps.common.entity.EntityStatus;
import com.tinysteps.patientservice.model.Gender;
import com.tinysteps.patientservice.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

        // Find by user ID
        Optional<Patient> findByUserId(UUID userId);

        // Find by gender
        List<Patient> findByGender(Gender gender);

        Page<Patient> findByGender(Gender gender, Pageable pageable);

        // Find by blood group
        List<Patient> findByBloodGroup(String bloodGroup);

        Page<Patient> findByBloodGroup(String bloodGroup, Pageable pageable);

        // Find by age range (calculated from date of birth)
        @Query("SELECT p FROM Patient p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) BETWEEN :minAge AND :maxAge")
        List<Patient> findByAgeBetween(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

        @Query("SELECT p FROM Patient p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) BETWEEN :minAge AND :maxAge")
        Page<Patient> findByAgeBetween(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge,
                        Pageable pageable);

        // Find by height range
        List<Patient> findByHeightCmBetween(Integer minHeight, Integer maxHeight);

        Page<Patient> findByHeightCmBetween(Integer minHeight, Integer maxHeight, Pageable pageable);

        // Find by weight range
        List<Patient> findByWeightKgBetween(BigDecimal minWeight, BigDecimal maxWeight);

        Page<Patient> findByWeightKgBetween(BigDecimal minWeight, BigDecimal maxWeight, Pageable pageable);

        // Find by date of birth range
        List<Patient> findByDateOfBirthBetween(Date startDate, Date endDate);

        Page<Patient> findByDateOfBirthBetween(Date startDate, Date endDate, Pageable pageable);

        // Validation methods
        boolean existsByUserId(UUID userId);

        // Count methods
        long countByGender(Gender gender);

        long countByBloodGroup(String bloodGroup);

        @Query("SELECT COUNT(p) FROM Patient p WHERE YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth) BETWEEN :minAge AND :maxAge")
        long countByAgeBetween(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);

        // Statistics methods
        @Query("SELECT p.bloodGroup, COUNT(p) FROM Patient p WHERE p.bloodGroup IS NOT NULL GROUP BY p.bloodGroup ORDER BY COUNT(p) DESC")
        List<Object[]> getBloodGroupStatistics();

        @Query("SELECT p.gender, COUNT(p) FROM Patient p WHERE p.gender IS NOT NULL GROUP BY p.gender")
        List<Object[]> getGenderStatistics();

        @Query("SELECT AVG(YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth)) FROM Patient p WHERE p.dateOfBirth IS NOT NULL")
        Double getAverageAge();

        @Query("SELECT AVG(p.heightCm) FROM Patient p WHERE p.heightCm IS NOT NULL")
        Double getAverageHeight();

        @Query("SELECT AVG(p.weightKg) FROM Patient p WHERE p.weightKg IS NOT NULL")
        BigDecimal getAverageWeight();

        // Find distinct values
        @Query("SELECT DISTINCT p.bloodGroup FROM Patient p WHERE p.bloodGroup IS NOT NULL ORDER BY p.bloodGroup")
        List<String> findDistinctBloodGroups();

        // Additional search methods
        @Query("SELECT p FROM Patient p WHERE (YEAR(CURRENT_DATE) - YEAR(p.dateOfBirth)) BETWEEN :minAge AND :maxAge")
        Page<Patient> findByAgeRange(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge,
                        Pageable pageable);

        @Query("SELECT p FROM Patient p WHERE p.heightCm BETWEEN :minHeight AND :maxHeight")
        List<Patient> findByHeightRange(@Param("minHeight") Integer minHeight, @Param("maxHeight") Integer maxHeight);

        @Query("SELECT p FROM Patient p WHERE p.heightCm BETWEEN :minHeight AND :maxHeight")
        Page<Patient> findByHeightRange(@Param("minHeight") Integer minHeight, @Param("maxHeight") Integer maxHeight,
                        Pageable pageable);

        @Query("SELECT p FROM Patient p WHERE p.weightKg BETWEEN :minWeight AND :maxWeight")
        List<Patient> findByWeightRange(@Param("minWeight") BigDecimal minWeight,
                        @Param("maxWeight") BigDecimal maxWeight);

        @Query("SELECT p FROM Patient p WHERE p.weightKg BETWEEN :minWeight AND :maxWeight")
        Page<Patient> findByWeightRange(@Param("minWeight") BigDecimal minWeight,
                        @Param("maxWeight") BigDecimal maxWeight,
                        Pageable pageable);

        @Query("SELECT p FROM Patient p WHERE p.dateOfBirth BETWEEN :startDate AND :endDate")
        List<Patient> findByDateOfBirthRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

        @Query("SELECT p FROM Patient p WHERE p.dateOfBirth BETWEEN :startDate AND :endDate")
        Page<Patient> findByDateOfBirthRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate,
                        Pageable pageable);

        // Branch-based filtering methods
        List<Patient> findByBranchId(UUID branchId);

        Page<Patient> findByBranchId(UUID branchId, Pageable pageable);

        // Include patients with NULL branchId (legacy patients) in branch-specific
        // queries
        List<Patient> findByBranchIdOrBranchIdIsNull(UUID branchId);

        Page<Patient> findByBranchIdOrBranchIdIsNull(UUID branchId, Pageable pageable);

        List<Patient> findByBranchIdIn(List<UUID> branchIds);

        Page<Patient> findByBranchIdIn(List<UUID> branchIds, Pageable pageable);

        long countByBranchId(UUID branchId);

        long countByBranchIdIn(List<UUID> branchIds);

        // Soft delete methods - find only active entities
        @Query("SELECT p FROM Patient p WHERE p.status = :status")
        List<Patient> findByStatus(@Param("status") EntityStatus status);

        @Query("SELECT p FROM Patient p WHERE p.status = :status")
        Page<Patient> findByStatus(@Param("status") EntityStatus status, Pageable pageable);

        @Query("SELECT p FROM Patient p WHERE p.userId = :userId AND p.status = :status")
        Optional<Patient> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") EntityStatus status);

        @Query("SELECT p FROM Patient p WHERE p.branchId = :branchId AND p.status = :status")
        List<Patient> findByBranchIdAndStatus(@Param("branchId") UUID branchId, @Param("status") EntityStatus status);

        @Query("SELECT p FROM Patient p WHERE p.branchId = :branchId AND p.status = :status")
        Page<Patient> findByBranchIdAndStatus(@Param("branchId") UUID branchId, @Param("status") EntityStatus status,
                        Pageable pageable);

        @Query("SELECT p FROM Patient p WHERE p.branchId IN :branchIds AND p.status = :status")
        List<Patient> findByBranchIdInAndStatus(@Param("branchIds") List<UUID> branchIds,
                        @Param("status") EntityStatus status);

        @Query("SELECT p FROM Patient p WHERE p.branchId IN :branchIds AND p.status = :status")
        Page<Patient> findByBranchIdInAndStatus(@Param("branchIds") List<UUID> branchIds,
                        @Param("status") EntityStatus status, Pageable pageable);

        // Count methods with status
        @Query("SELECT COUNT(p) FROM Patient p WHERE p.branchId = :branchId AND p.status = :status")
        long countByBranchIdAndStatus(@Param("branchId") UUID branchId, @Param("status") EntityStatus status);

        @Query("SELECT COUNT(p) FROM Patient p WHERE p.branchId IN :branchIds AND p.status = :status")
        long countByBranchIdInAndStatus(@Param("branchIds") List<UUID> branchIds, @Param("status") EntityStatus status);
}
