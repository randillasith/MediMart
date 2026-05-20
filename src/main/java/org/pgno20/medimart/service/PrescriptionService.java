package org.pgno20.medimart.service;

import org.pgno20.medimart.model.Prescription;
import org.pgno20.medimart.repository.PrescriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

/**
 * PRESCRIPTION SERVICE
 * =====================
 * OOP Concepts demonstrated:
 * - Abstraction: controller doesn't know HOW things work, just WHAT to call
 * - Encapsulation: all logic is inside this class, hidden from controller
 *
 * File location: src/main/java/org/pgno20/medimart/service/PrescriptionService.java
 *
 * ─────────────────────────────────────────────────────
 * HOW FILE HANDLING WORKS (for your viva):
 * ─────────────────────────────────────────────────────
 * 1. User picks a file in the HTML form (type="file")
 * 2. Browser sends it as multipart/form-data to our API
 * 3. Spring Boot receives it as a MultipartFile object
 * 4. We use Files.copy() to save it to the "uploads/" folder on disk
 * 5. We store only the FILENAME in the database (not the whole file)
 * 6. When we want to show the file, we serve it via the /uploads/ URL
 *    (WebConfig already maps /uploads/** to the uploads folder)
 * ─────────────────────────────────────────────────────
 */
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    // Constructor injection (same pattern as MedicineService)
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    // ─── CREATE ───────────────────────────────────────────────────────

    @Transactional
    public Prescription create(String patientName, String doctorName, String medicineDetails,
                               String dosage, String instructions, LocalDate prescriptionDate,
                               MultipartFile file, String submittedByName, String submittedByEmail) {
        validatePrescription(patientName, doctorName, medicineDetails, dosage, prescriptionDate, file);

        // 1. Build the Prescription object
        Prescription prescription = new Prescription(
                patientName, doctorName, medicineDetails,
                dosage, instructions, prescriptionDate
        );
        prescription.setStatus("PENDING");
        prescription.setSubmittedByName(submittedByName);
        prescription.setSubmittedByEmail(submittedByEmail);
        prescription.setPrescriptionId("RX-TEMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // 2. Save first to get the auto-generated database ID
        Prescription saved = prescriptionRepository.save(prescription);

        // 3. Generate a human-readable prescription ID like "RX001"
        saved.setPrescriptionId(String.format("RX%03d", saved.getId()));

        // 4. Handle file upload (if a file was provided)
        if (file != null && !file.isEmpty()) {
            String filename = saveFile(file, saved.getPrescriptionId());
            saved.setFileName(filename);
        }

        // 5. Save again with the prescription ID and filename
        return prescriptionRepository.save(saved);
    }

    // ─── READ (all / search / single) ─────────────────────────────────

    public Page<Prescription> getAll(Pageable pageable) {
        return prescriptionRepository.findAll(pageable);
    }

    public Page<Prescription> search(String search, String status, Pageable pageable) {
        String cleanSearch = (search != null && !search.isBlank()) ? search : null;
        String cleanStatus = (status != null && !status.isBlank()) ? status : null;
        return prescriptionRepository.searchPrescriptions(cleanSearch, cleanStatus, pageable);
    }

    public Page<Prescription> findForMember(String email, Pageable pageable) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Member email is required");
        }
        return prescriptionRepository.findBySubmittedByEmailIgnoreCase(email, pageable);
    }

    public Prescription getById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found with id: " + id));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────

    @Transactional
    public Prescription update(Long id, String patientName, String doctorName, String medicineDetails,
                               String dosage, String instructions, LocalDate prescriptionDate,
                               String status, MultipartFile file) {

        // 1. Find existing prescription
        Prescription existing = getById(id);

        // 2. Update fields
        existing.setPatientName(patientName);
        existing.setDoctorName(doctorName);
        existing.setMedicineDetails(medicineDetails);
        existing.setDosage(dosage);
        existing.setInstructions(instructions);
        existing.setPrescriptionDate(prescriptionDate);
        if (status != null && !status.isBlank() && isAllowedStatus(status)) {
            existing.setStatus(status);
        }

        // 3. If a new file is uploaded, replace the old one
        if (file != null && !file.isEmpty()) {
            // Delete the old file first (optional, keeps disk clean)
            deleteOldFile(existing.getFileName());
            // Save the new file
            String filename = saveFile(file, existing.getPrescriptionId());
            existing.setFileName(filename);
        }

        return prescriptionRepository.save(existing);
    }

    @Transactional
    public Prescription review(Long id, String status, String rejectionReason, String reviewer) {
        Prescription existing = getById(id);
        if ("APPROVED".equalsIgnoreCase(status)) {
            existing.approve(reviewer);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                throw new IllegalArgumentException("Rejection reason is required");
            }
            existing.reject(rejectionReason.trim(), reviewer);
        } else {
            throw new IllegalArgumentException("Status must be APPROVED or REJECTED");
        }
        return prescriptionRepository.save(existing);
    }

    // ─── DELETE ───────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        Prescription prescription = getById(id);

        // Also delete the uploaded file from disk
        deleteOldFile(prescription.getFileName());

        // Delete from database
        prescriptionRepository.deleteById(id);
    }

    // ─── FILE HANDLING HELPERS ────────────────────────────────────────

    /**
     * HOW saveFile() WORKS:
     * 1. Get the original filename (e.g. "myprescription.jpg")
     * 2. Extract the extension (e.g. ".jpg")
     * 3. Create a unique filename: "RX001_abc12345.jpg"
     *    (UUID prevents two files from having the same name)
     * 4. Ensure the "uploads/" folder exists on disk
     * 5. Copy the file bytes from memory to disk using Files.copy()
     * 6. Return the filename so we can store it in the database
     */
    private String saveFile(MultipartFile file, String prescriptionId) {
        try {
            validateFile(file);
            // Get file extension
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            // Build unique filename
            String uniquePart = UUID.randomUUID().toString().substring(0, 8);
            String filename = prescriptionId + "_" + uniquePart + extension;

            // Ensure uploads folder exists
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Write file bytes to disk
            Path destination = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return filename;

        } catch (IOException e) {
            throw new RuntimeException("Could not save prescription file: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a file from the uploads folder on disk.
     * Called when updating or deleting a prescription.
     */
    private void deleteOldFile(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path filePath = Paths.get("uploads").resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but don't fail — the important operation (DB update) should continue
            System.err.println("Warning: could not delete old file: " + filename);
        }
    }

    // ─── Stats ────────────────────────────────────────────────────────

    public long countActive() {
        return prescriptionRepository.countByStatus("APPROVED");
    }

    public long countTotal() {
        return prescriptionRepository.count();
    }

    public long countPending() {
        return prescriptionRepository.countByStatus("PENDING");
    }

    public long countRejected() {
        return prescriptionRepository.countByStatus("REJECTED");
    }

    public Prescription getByPrescriptionId(String prescriptionId) {
        return prescriptionRepository.findByPrescriptionId(prescriptionId).orElse(null);
    }

    private void validatePrescription(String patientName, String doctorName, String medicineDetails,
                                      String dosage, LocalDate prescriptionDate, MultipartFile file) {
        if (isBlank(patientName) || isBlank(doctorName) || isBlank(medicineDetails) || isBlank(dosage)) {
            throw new IllegalArgumentException("Patient, doctor, medicine and dosage are required");
        }
        if (prescriptionDate == null) {
            throw new IllegalArgumentException("Prescription date is required");
        }
        if (prescriptionDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Prescription date cannot be in the future");
        }
        if (prescriptionDate.isBefore(LocalDate.now().minusMonths(6))) {
            throw new IllegalArgumentException("Prescription must be issued within the last 6 months");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Prescription file is required");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Prescription file must be 10MB or smaller");
        }
        String contentType = file.getContentType();
        if (!("application/pdf".equals(contentType)
                || "image/jpeg".equals(contentType)
                || "image/png".equals(contentType))) {
            throw new IllegalArgumentException("Only PDF, JPEG and PNG prescription files are allowed");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isAllowedStatus(String status) {
        return "PENDING".equals(status) || "APPROVED".equals(status) || "REJECTED".equals(status);
    }
}
