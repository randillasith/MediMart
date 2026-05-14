package org.pgno20.medimart.controller;

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
    public ResponseEntity<Supplier> create(@Valid @RequestBody Supplier supplier) {
        Supplier created = supplierService.createSupplier(supplier);
        URI location = URI.create("/api/suppliers/" + created.getId());
        return ResponseEntity.created(location).body(created);  // 201 instead of 200
    }

    // Read all
    @GetMapping
    public ResponseEntity<List<Supplier>> getAll() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    // Read by id — returns 404 if not found
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getById(@PathVariable String id) {
        try {
            Supplier supplier = supplierService.getSupplierById(id);
            return ResponseEntity.ok(supplier);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> update(@PathVariable String id, @Valid @RequestBody Supplier supplier) {
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
    public ResponseEntity<List<Supplier>> searchByName(@RequestParam String q) {
        return ResponseEntity.ok(supplierService.searchByName(q));
    }

    // Search by medicine
    @GetMapping("/search/medicine")
    public ResponseEntity<List<Supplier>> searchByMedicine(@RequestParam String q) {
        return ResponseEntity.ok(supplierService.searchByMedicineKeyword(q));
    }
}