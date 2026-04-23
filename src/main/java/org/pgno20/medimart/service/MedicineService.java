package org.pgno20.medimart.service;

import org.pgno20.medimart.dto.MedicineCreateRequest;
import org.pgno20.medimart.dto.MedicineResponse;
import org.pgno20.medimart.dto.MedicineUpdateRequest;
import org.pgno20.medimart.model.*;
import org.pgno20.medimart.repository.CategoryRepository;
import org.pgno20.medimart.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;

    public MedicineService(MedicineRepository medicineRepository, CategoryRepository categoryRepository) {
        this.medicineRepository = medicineRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public MedicineResponse create(MedicineCreateRequest req) {
        if (medicineRepository.findBySku(req.getSku()).isPresent()) {
            throw new IllegalArgumentException("SKU already exists: " + req.getSku());
        }

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Medicine medicine = buildMedicineByType(req.getType());
        medicine.setSku(req.getSku());
        medicine.setName(req.getName());
        medicine.setBrand(req.getBrand());
        medicine.setDosage(req.getDosage());
        medicine.setPrice(req.getPrice());
        medicine.setStockQty(req.getStockQty());
        medicine.setExpiryDate(req.getExpiryDate());
        medicine.setCategory(category);

        // Business rules
        if (medicine.isExpired()) {
            medicine.setStatus("DISCONTINUED");
        } else {
            medicine.normalizeStatusFromStock();
        }

        Medicine saved = medicineRepository.save(medicine);
        return toResponse(saved);
    }

    public List<MedicineResponse> listAll() {
        return medicineRepository.findAll().stream().map(this::toResponse).toList();
    }

    public MedicineResponse getById(Long id) {
        Medicine m = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));
        return toResponse(m);
    }

    public List<MedicineResponse> search(String name, String brand, Long categoryId, String status) {
        // simple search priority (you can improve later)
        if (name != null && !name.isBlank()) {
            return medicineRepository.findByNameContainingIgnoreCase(name).stream().map(this::toResponse).toList();
        }
        if (brand != null && !brand.isBlank()) {
            return medicineRepository.findByBrandContainingIgnoreCase(brand).stream().map(this::toResponse).toList();
        }
        if (categoryId != null) {
            return medicineRepository.findByCategory_Id(categoryId).stream().map(this::toResponse).toList();
        }
        if (status != null && !status.isBlank()) {
            return medicineRepository.findByStatus(status).stream().map(this::toResponse).toList();
        }
        return listAll();
    }

    @Transactional
    public MedicineResponse update(Long id, MedicineUpdateRequest req) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        medicine.setName(req.getName());
        medicine.setBrand(req.getBrand());
        medicine.setDosage(req.getDosage());
        medicine.setPrice(req.getPrice());
        medicine.setStockQty(req.getStockQty());
        medicine.setExpiryDate(req.getExpiryDate());
        medicine.setCategory(category);

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            medicine.setStatus(req.getStatus());
        }

        // Business rules
        if (medicine.isExpired()) {
            medicine.setStatus("DISCONTINUED");
        } else {
            medicine.normalizeStatusFromStock();
        }

        return toResponse(medicineRepository.save(medicine));
    }

    @Transactional
    public void discontinue(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));
        medicine.setStatus("DISCONTINUED");
        medicineRepository.save(medicine);
    }

    private Medicine buildMedicineByType(String type) {
        if (type == null) throw new IllegalArgumentException("type is required");

        return switch (type.trim().toUpperCase()) {
            case "OTC" -> new OTCMedicine();
            case "PRESCRIPTION" -> new PrescriptionMedicine();
            default -> throw new IllegalArgumentException("Invalid type. Use OTC or PRESCRIPTION");
        };
    }

    private MedicineResponse toResponse(Medicine m) {
        MedicineResponse r = new MedicineResponse();
        r.setId(m.getId());
        r.setSku(m.getSku());
        r.setName(m.getName());
        r.setBrand(m.getBrand());
        r.setDosage(m.getDosage());
        r.setPrice(m.getPrice());
        r.setStockQty(m.getStockQty());
        r.setExpiryDate(m.getExpiryDate());
        r.setPrescriptionRequired(Boolean.TRUE.equals(m.getPrescriptionRequired()));
        r.setStatus(m.getStatus());
        r.setTypeLabel(m.getTypeLabel());
        r.setCategoryId(m.getCategory().getId());
        r.setCategoryName(m.getCategory().getName());
        return r;
    }
}