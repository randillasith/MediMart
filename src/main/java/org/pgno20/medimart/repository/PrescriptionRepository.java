package org.pgno20.medimart.repository;

import org.pgno20.medimart.model.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * PRESCRIPTION REPOSITORY
 * ========================
 * Extends JpaRepository → gives us free CRUD methods:
 *   - save(), findById(), findAll(), deleteById(), count()
 *
 * We only write custom queries on top of that.
 *
 * File location: src/main/java/org/pgno20/medimart/repository/PrescriptionRepository.java
 */
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // Find by the human-readable ID like "RX001"
    Optional<Prescription> findByPrescriptionId(String prescriptionId);

    // Search by patient name (case-insensitive, partial match)
    Page<Prescription> findByPatientNameContainingIgnoreCase(String patientName, Pageable pageable);

    // Search by doctor name
    Page<Prescription> findByDoctorNameContainingIgnoreCase(String doctorName, Pageable pageable);

    // Search by status
    Page<Prescription> findByStatus(String status, Pageable pageable);

    Page<Prescription> findBySubmittedByEmailIgnoreCase(String submittedByEmail, Pageable pageable);

    java.util.List<Prescription> findBySubmittedByEmailIgnoreCase(String submittedByEmail);

    // Combined search: patient name OR doctor name OR status
    @Query("SELECT p FROM Prescription p WHERE " +
           "(:search IS NULL OR LOWER(p.patientName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.doctorName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Prescription> searchPrescriptions(
            @Param("search") String search,
            @Param("status") String status,
            Pageable pageable);

    // Count how many prescriptions exist (for stats)
    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.status = 'ACTIVE'")
    long countActive();

    long countByStatus(String status);

    /** Check if any prescription with the given status exists for a customer email.
     *  Used by OrderService to gate status updates on prescription-required orders. */
    boolean existsBySubmittedByEmailIgnoreCaseAndStatus(String email, String status);

    /** Find prescriptions for a specific email filtered by status (used for Rx approval check). */
    Page<Prescription> findBySubmittedByEmailIgnoreCaseAndStatus(
            String submittedByEmail, String status, Pageable pageable);
}
