package org.pgno20.medimart.controller;

import jakarta.validation.Valid;
import org.pgno20.medimart.dto.MedicineCreateRequest;
import org.pgno20.medimart.dto.MedicineResponse;
import org.pgno20.medimart.dto.MedicineUpdateRequest;
import org.pgno20.medimart.service.MedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    // Create
    @PostMapping
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody MedicineCreateRequest req) {
        return ResponseEntity.ok(medicineService.create(req));
    }

    // Read all / search
    @GetMapping
    public ResponseEntity<List<MedicineResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(medicineService.search(name, brand, categoryId, status));
    }

    // Read one
    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getById(id));
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<MedicineResponse> update(@PathVariable Long id, @Valid @RequestBody MedicineUpdateRequest req) {
        return ResponseEntity.ok(medicineService.update(id, req));
    }

    // Delete (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> discontinue(@PathVariable Long id) {
        medicineService.discontinue(id);
        return ResponseEntity.noContent().build();
    }
}