package org.pgno20.medimart.controller;

import org.pgno20.medimart.dto.SupplierResponse;
import org.pgno20.medimart.entity.Supplier;
import org.pgno20.medimart.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    // Create
    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody Supplier supplier) {
        SupplierResponse created = supplierService.createSupplier(supplier);
        URI location = URI.create("/api/suppliers/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    // Read all
    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAll() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    // Read by id
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(supplierService.getSupplierResponseById(id));
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(@PathVariable String id, @Valid @RequestBody Supplier supplier) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, supplier));
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    // Search by name
    @GetMapping("/search/name")
    public ResponseEntity<List<SupplierResponse>> searchByName(@RequestParam String q) {
        return ResponseEntity.ok(supplierService.searchByName(q));
    }

    // Search by medicine
    @GetMapping("/search/medicine")
    public ResponseEntity<List<SupplierResponse>> searchByMedicine(@RequestParam String q) {
        return ResponseEntity.ok(supplierService.searchByMedicineKeyword(q));
    }
}