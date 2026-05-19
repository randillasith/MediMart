package org.pgno20.medimart.service;

import org.pgno20.medimart.entity.Supplier;
import org.pgno20.medimart.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    // Create
    public Supplier createSupplier(Supplier supplier) {
        if (supplier.getId() == null || supplier.getId().isBlank()) {
            supplier.setId(generateNextId());
        }
        return supplierRepository.save(supplier);
    }

    // Read all
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    // Read by id
    public Supplier getSupplierById(String id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));
    }

    // Update
    public Supplier updateSupplier(String id, Supplier updated) {
        Supplier existing = getSupplierById(id);

        existing.setName(updated.getName());
        existing.setType(updated.getType());

        existing.setContact(updated.getContact());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setMedicinesSupplied(updated.getMedicinesSupplied());

        return supplierRepository.save(existing);
    }

    // Delete
    public void deleteSupplier(String id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Supplier not found: " + id);
        }
        supplierRepository.deleteById(id);
    }

    // Search
    public List<Supplier> searchByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }

    public java.util.Optional<Supplier> getSupplierByEmail(String email) {
        return supplierRepository.findByEmail(email);
    }

    public List<Supplier> searchByMedicineKeyword(String keyword) {
        return supplierRepository.findByMedicinesSuppliedContainingIgnoreCase(keyword);
    }

    // Safe ID generator: SUP001, SUP002...
    private String generateNextId() {
        String maxId = supplierRepository.findMaxId();
        if (maxId == null || maxId.isBlank()) {
            return "SUP001";
        }
        int currentNum = Integer.parseInt(maxId.substring(3));
        return String.format("SUP%03d", currentNum + 1);
    }
}