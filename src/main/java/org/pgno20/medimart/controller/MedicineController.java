package org.pgno20.medimart.controller;

import jakarta.validation.Valid;
import org.pgno20.medimart.dto.MedicineCreateRequest;
import org.pgno20.medimart.dto.MedicineResponse;
import org.pgno20.medimart.dto.MedicineUpdateRequest;
import org.pgno20.medimart.service.MedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;
import java.util.Map;

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

    @GetMapping
    public ResponseEntity<Page<MedicineResponse>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(medicineService.search(name, brand, categoryId, status, pageable));
    }

    @GetMapping("/storefront")
    public ResponseEntity<Page<org.pgno20.medimart.dto.StorefrontMedicineDTO>> listStorefront(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(medicineService.searchStorefrontMedicines(search, pageable));
        }
        return ResponseEntity.ok(medicineService.getStorefrontMedicines(pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(medicineService.getStats());
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

    // Upload Image
    @PostMapping("/{id}/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable Long id, 
            @org.springframework.web.bind.annotation.RequestParam("image") org.springframework.web.multipart.MultipartFile file) {
        String filename = medicineService.uploadImage(id, file);
        return ResponseEntity.ok(Map.of("imageUrl", filename));
    }
}