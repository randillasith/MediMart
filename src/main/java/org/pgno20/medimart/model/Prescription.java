package org.pgno20.medimart.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PRESCRIPTION MODEL
 * ===================
 * OOP Concepts demonstrated:
 * - Encapsulation: all fields are private, accessed via public getters/setters
 * - Abstraction: data is hidden; only necessary info is exposed
 *
 * File location: src/main/java/org/pgno20/medimart/model/Prescription.java
 */
@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // Auto-generated primary key

    @Column(unique = true, length = 20)   // nullable allowed: ID is assigned AFTER first save
    private String prescriptionId;      // e.g. RX001, RX002

    @Column(nullable = false, length = 100)
    private String patientName;         // Patient's full name

    @Column(nullable = false, length = 100)
    private String doctorName;          // Doctor's full name

    @Column(nullable = false, length = 200)
    private String medicineDetails;     // e.g. "Paracetamol 500mg, Amoxicillin 250mg"

    @Column(nullable = false, length = 100)
    private String dosage;              // e.g. "1 tablet 3 times daily"

    @Column(length = 500)
    private String instructions;        // Special instructions from the doctor

    @Column(nullable = false)
    private LocalDate prescriptionDate; // Date of prescription

    @Column(length = 255)
    private String fileName;            // Uploaded prescription image filename

    @Column(nullable = false, length = 20)
    private String status = "PENDING";   // PENDING / APPROVED / REJECTED

    @Column(length = 200)
    private String submittedByName;

    @Column(length = 200)
    private String submittedByEmail;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 200)
    private String reviewedBy;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    // ─── Constructors ─────────────────────────────────────────────────
    // Required by JPA
    public Prescription() {}

    // Convenience constructor (used in service)
    public Prescription(String patientName, String doctorName, String medicineDetails,
                        String dosage, String instructions, LocalDate prescriptionDate) {
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.medicineDetails = medicineDetails;
        this.dosage = dosage;
        this.instructions = instructions;
        this.prescriptionDate = prescriptionDate;
    }

    // ─── Business logic helpers ────────────────────────────────────────
    /**
     * Checks if the prescription is older than 6 months (expired).
     * This is a BUSINESS RULE encapsulated inside the model.
     */
    public boolean isExpired() {
        if (prescriptionDate == null) return false;
        return prescriptionDate.isBefore(LocalDate.now().minusMonths(6));
    }

    /**
     * Returns a human-readable status label.
     * Demonstrates abstraction — caller doesn't need to know the logic.
     */
    public String getStatusLabel() {
        if (isExpired() && "APPROVED".equals(status)) {
            return "EXPIRED";
        }
        return status;
    }

    public void approve(String reviewer) {
        this.status = "APPROVED";
        this.rejectionReason = null;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(String reason, String reviewer) {
        this.status = "REJECTED";
        this.rejectionReason = reason;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now();
    }

    @PrePersist
    void prePersist() {
        if (this.status == null || this.status.isBlank()) this.status = "PENDING";
        if (this.submittedAt == null) this.submittedAt = LocalDateTime.now();
    }

    // ─── Getters (Encapsulation: controlled read access) ──────────────
    public Long getId()                  { return id; }
    public String getPrescriptionId()    { return prescriptionId; }
    public String getPatientName()       { return patientName; }
    public String getDoctorName()        { return doctorName; }
    public String getMedicineDetails()   { return medicineDetails; }
    public String getDosage()            { return dosage; }
    public String getInstructions()      { return instructions; }
    public LocalDate getPrescriptionDate(){ return prescriptionDate; }
    public String getFileName()          { return fileName; }
    public String getStatus()            { return status; }
    public String getSubmittedByName()   { return submittedByName; }
    public String getSubmittedByEmail()  { return submittedByEmail; }
    public String getRejectionReason()   { return rejectionReason; }
    public String getReviewedBy()        { return reviewedBy; }
    public LocalDateTime getSubmittedAt(){ return submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }

    // ─── Setters (Encapsulation: controlled write access) ─────────────
    public void setId(Long id)                      { this.id = id; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }
    public void setPatientName(String patientName)  { this.patientName = patientName; }
    public void setDoctorName(String doctorName)    { this.doctorName = doctorName; }
    public void setMedicineDetails(String medicineDetails) { this.medicineDetails = medicineDetails; }
    public void setDosage(String dosage)            { this.dosage = dosage; }
    public void setInstructions(String instructions){ this.instructions = instructions; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }
    public void setFileName(String fileName)        { this.fileName = fileName; }
    public void setStatus(String status)            { this.status = status; }
    public void setSubmittedByName(String submittedByName) { this.submittedByName = submittedByName; }
    public void setSubmittedByEmail(String submittedByEmail) { this.submittedByEmail = submittedByEmail; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
