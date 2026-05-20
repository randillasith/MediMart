package org.pgno20.medimart.service;

import org.pgno20.medimart.dto.SupplierResponse;
import org.pgno20.medimart.entity.Supplier;
import org.pgno20.medimart.repository.SupplierRepository;
import org.pgno20.medimart.repository.UserRepository;
import org.pgno20.medimart.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SupplierService(SupplierRepository supplierRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Create
    public SupplierResponse createSupplier(Supplier supplier) {
        if (supplier.getId() == null || supplier.getId().isBlank()) {
            supplier.setId(generateNextId());
        }
        Supplier saved = supplierRepository.save(supplier);
        
        // Auto-create User account for supplier
        if (supplier.getEmail() != null && !supplier.getEmail().isBlank()) {
            if (!userRepository.existsByEmail(supplier.getEmail())) {
                User sUser = new User();
                sUser.setFullName(supplier.getName() + " Admin");
                sUser.setEmail(supplier.getEmail());
                sUser.setPassword(passwordEncoder.encode("Supplier@123")); // Default password
                sUser.setRoleName("ROLE_SUPPLIER");
                sUser.setGender("Other");
                sUser.setDob(java.time.LocalDate.now());
                sUser.setActive(true);
                userRepository.save(sUser);
            }
        }
        
        return toResponse(saved);
    }

    // Read all
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Read by id
    public Supplier getSupplierById(String id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + id));
    }

    // Read by id (DTO)
    public SupplierResponse getSupplierResponseById(String id) {
        return toResponse(getSupplierById(id));
    }

    // Update
    public SupplierResponse updateSupplier(String id, Supplier updated) {
        Supplier existing = getSupplierById(id);

        existing.setName(updated.getName());
        existing.setType(updated.getType());

        existing.setContact(updated.getContact());
        existing.setEmail(updated.getEmail());
        existing.setAddress(updated.getAddress());
        existing.setMedicinesSupplied(updated.getMedicinesSupplied());

        return toResponse(supplierRepository.save(existing));
    }

    // Delete
    public void deleteSupplier(String id) {
        if (!supplierRepository.existsById(id)) {
            throw new IllegalArgumentException("Supplier not found: " + id);
        }
        supplierRepository.deleteById(id);
    }

    // Search
    public List<SupplierResponse> searchByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public java.util.Optional<Supplier> getSupplierByEmail(String email) {
        return supplierRepository.findByEmail(email);
    }

    public List<SupplierResponse> searchByMedicineKeyword(String keyword) {
        return supplierRepository.findByMedicinesSuppliedContainingIgnoreCase(keyword).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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

    // Convert entity to DTO
    private SupplierResponse toResponse(Supplier supplier) {
        SupplierResponse r = new SupplierResponse();
        r.setId(supplier.getId());
        r.setName(supplier.getName());
        r.setType(supplier.getType());
        r.setContact(supplier.getContact());
        r.setEmail(supplier.getEmail());
        r.setAddress(supplier.getAddress());
        r.setMedicinesSupplied(supplier.getMedicinesSupplied());
        r.setSupplierCategory(supplier.getSupplierCategory());
        return r;
    }
}