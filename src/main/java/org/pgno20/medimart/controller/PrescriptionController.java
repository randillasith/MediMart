package org.pgno20.medimart.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.pgno20.medimart.model.Prescription;
import org.pgno20.medimart.service.PrescriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

/**
 * PRESCRIPTION CONTROLLER
 * ========================
 * This is a REST Controller — it handles HTTP requests and returns JSON.
 * The frontend HTML pages call these endpoints using JavaScript fetch().
 *
 * OOP Concepts:
 * - Abstraction: each method has a clear job, delegates details to the Service
 * - Encapsulation: the controller only knows WHAT the service does, not HOW
 *
 * File location:
 * src/main/java/org/pgno20/medimart/controller/PrescriptionController.java
 *
 * ─────────────────────────────────────────────────────
 * REST ENDPOINT SUMMARY:
 * ─────────────────────────────────────────────────────
 * POST /api/prescriptions → Create
 * GET /api/prescriptions → List all (paginated)
 * GET /api/prescriptions/{id} → Get one
 * PUT /api/prescriptions/{id} → Update
 * DELETE /api/prescriptions/{id} → Delete
 * GET /api/prescriptions/stats → Count stats
 * ─────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    // Constructor injection (same as MedicineController)
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────
    /**
     * POST /api/prescriptions
     * Creates a new prescription. Accepts multipart/form-data because we also
     * handle file uploads (the prescription image).
     *
     * @RequestParam reads individual form fields from the HTTP request.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Prescription> create(
            @RequestParam("patientName") String patientName,
            @RequestParam("doctorName") String doctorName,
            @RequestParam("medicineDetails") String medicineDetails,
            @RequestParam("dosage") String dosage,
            @RequestParam(value = "instructions", required = false) String instructions,
            @RequestParam("prescriptionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate prescriptionDate,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpServletRequest request) {

        HttpSession session = requireSession(request);
        String submittedByName = (String) session.getAttribute("userFullName");
        String submittedByEmail = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;

        Prescription created = prescriptionService.create(
                patientName, doctorName, medicineDetails,
                dosage, instructions, prescriptionDate, file,
                submittedByName, submittedByEmail);
        return ResponseEntity.ok(created);
    }

    // ─── READ ALL ─────────────────────────────────────────────────────────────
    /**
     * GET /api/prescriptions?search=John&status=ACTIVE&page=0&size=10
     * GET /api/prescriptions?email=user@example.com&status=APPROVED&size=1  ← used by admin Rx gate check
     * Returns a paginated list. Optional search by patient/doctor name or by customer email.
     */
    @GetMapping
    public ResponseEntity<Page<Prescription>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 10) Pageable pageable) {

        // If email is supplied → filter by that customer's email (Rx approval check by admin)
        if (email != null) {
            if (email.isBlank()) {
                return ResponseEntity.ok(Page.empty(pageable));
            }
            return ResponseEntity.ok(prescriptionService.findByEmail(email, status, pageable));
        }
        Page<Prescription> result = prescriptionService.search(search, status, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/mine")
    public ResponseEntity<Page<Prescription>> mine(
            @PageableDefault(size = 10) Pageable pageable,
            HttpServletRequest request) {
        requireSession(request);
        String email = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
        return ResponseEntity.ok(prescriptionService.findForMember(email, pageable));
    }

    // ─── READ ONE ─────────────────────────────────────────────────────────────
    /**
     * GET /api/prescriptions/5
     * Returns a single prescription by its database ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Prescription> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getById(id));
    }

    /**
     * GET /api/prescriptions/by-rx-id/RX001
     * Returns a single prescription by its custom prescriptionId string.
     */
    @GetMapping("/by-rx-id/{prescriptionId}")
    public ResponseEntity<Prescription> getByRxId(@PathVariable String prescriptionId) {
        Prescription p = prescriptionService.getByPrescriptionId(prescriptionId);
        if (p == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(p);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────
    /**
     * PUT /api/prescriptions/5
     * Updates an existing prescription. Can optionally upload a new file.
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Prescription> update(
            @PathVariable Long id,
            @RequestParam("patientName") String patientName,
            @RequestParam("doctorName") String doctorName,
            @RequestParam("medicineDetails") String medicineDetails,
            @RequestParam("dosage") String dosage,
            @RequestParam(value = "instructions", required = false) String instructions,
            @RequestParam("prescriptionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate prescriptionDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Prescription updated = prescriptionService.update(
                id, patientName, doctorName, medicineDetails,
                dosage, instructions, prescriptionDate, status, file);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<Prescription> review(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        String reviewer = "Admin";
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userFullName") != null) {
            reviewer = (String) session.getAttribute("userFullName");
        }
        Prescription reviewed = prescriptionService.review(
                id,
                body.get("status"),
                body.get("rejectionReason"),
                reviewer);
        return ResponseEntity.ok(reviewed);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────
    /**
     * DELETE /api/prescriptions/5
     * Permanently deletes the prescription and its uploaded file from disk.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        prescriptionService.delete(id);
        return ResponseEntity.noContent().build(); // Returns 204 No Content
    }

    // ─── STATS ───────────────────────────────────────────────────────────────
    /**
     * GET /api/prescriptions/stats
     * Returns count stats for the dashboard (if you want to add a tile).
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of(
                "total", prescriptionService.countTotal(),
                "approved", prescriptionService.countActive(),
                "pending", prescriptionService.countPending(),
                "rejected", prescriptionService.countRejected()));
    }

    private HttpSession requireSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            throw new IllegalArgumentException("You must log in before submitting prescriptions");
        }
        return session;
    }
}
