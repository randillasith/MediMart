package org.pgno20.medimart.controller;

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
 * File location: src/main/java/org/pgno20/medimart/controller/PrescriptionController.java
 *
 * ─────────────────────────────────────────────────────
 * REST ENDPOINT SUMMARY:
 * ─────────────────────────────────────────────────────
 * POST   /api/prescriptions          → Create
 * GET    /api/prescriptions          → List all (paginated)
 * GET    /api/prescriptions/{id}     → Get one
 * PUT    /api/prescriptions/{id}     → Update
 * DELETE /api/prescriptions/{id}     → Delete
 * GET    /api/prescriptions/stats    → Count stats
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
            @RequestParam("patientName")      String patientName,
            @RequestParam("doctorName")       String doctorName,
            @RequestParam("medicineDetails")  String medicineDetails,
            @RequestParam("dosage")           String dosage,
            @RequestParam(value = "instructions", required = false) String instructions,
            @RequestParam("prescriptionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate prescriptionDate,
            @RequestParam(value = "file",     required = false) MultipartFile file) {

        Prescription created = prescriptionService.create(
                patientName, doctorName, medicineDetails,
                dosage, instructions, prescriptionDate, file
        );
        return ResponseEntity.ok(created);
    }

    // ─── READ ALL ─────────────────────────────────────────────────────────────
    /**
     * GET /api/prescriptions?search=John&status=ACTIVE&page=0&size=10
     * Returns a paginated list. Optional search by patient/doctor name.
     */
    @GetMapping
    public ResponseEntity<Page<Prescription>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<Prescription> result = prescriptionService.search(search, status, pageable);
        return ResponseEntity.ok(result);
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

    // ─── UPDATE ──────────────────────────────────────────────────────────────
    /**
     * PUT /api/prescriptions/5
     * Updates an existing prescription. Can optionally upload a new file.
     */
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Prescription> update(
            @PathVariable Long id,
            @RequestParam("patientName")      String patientName,
            @RequestParam("doctorName")       String doctorName,
            @RequestParam("medicineDetails")  String medicineDetails,
            @RequestParam("dosage")           String dosage,
            @RequestParam(value = "instructions", required = false) String instructions,
            @RequestParam("prescriptionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate prescriptionDate,
            @RequestParam(value = "status",   required = false) String status,
            @RequestParam(value = "file",     required = false) MultipartFile file) {

        Prescription updated = prescriptionService.update(
                id, patientName, doctorName, medicineDetails,
                dosage, instructions, prescriptionDate, status, file
        );
        return ResponseEntity.ok(updated);
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
                "total",  prescriptionService.countTotal(),
                "active", prescriptionService.countActive()
        ));
    }
}
