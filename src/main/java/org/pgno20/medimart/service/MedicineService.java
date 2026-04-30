package org.pgno20.medimart.service;

import org.pgno20.medimart.dto.MedicineCreateRequest;
import org.pgno20.medimart.dto.MedicineResponse;
import org.pgno20.medimart.dto.MedicineUpdateRequest;
import org.pgno20.medimart.model.*;
import org.pgno20.medimart.repository.CategoryRepository;
import org.pgno20.medimart.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

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


        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Medicine medicine = buildMedicineByType(req.getType());
        // Shorten the temp string so it fits in the 40-char limit
        medicine.setSku("TEMP-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        medicine.setName(req.getName());
        medicine.setBrand(req.getBrand());
        medicine.setDosage(formatDosage(req.getDosage()));
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

        // First save to generate the ID
        Medicine saved = medicineRepository.save(medicine);
        
        // Update SKU with the MED001 format based on the generated ID
        saved.setSku(String.format("MED%03d", saved.getId()));
        saved = medicineRepository.save(saved);
        
        return toResponse(saved);
    }

    public Page<MedicineResponse> listAll(Pageable pageable) {
        return medicineRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<org.pgno20.medimart.dto.StorefrontMedicineDTO> getStorefrontMedicines(Pageable pageable) {
        return medicineRepository.getStorefrontMedicines(pageable);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", medicineRepository.countAll());
        stats.put("lowStock",      medicineRepository.countLowStock());
        stats.put("outOfStock",    medicineRepository.countOutOfStock());
        stats.put("totalValue",    medicineRepository.sumTotalValue());
        return stats;
    }

    public MedicineResponse getById(Long id) {
        Medicine m = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));
        return toResponse(m);
    }

    public Page<MedicineResponse> search(String name, String brand, Long categoryId, String status, Pageable pageable) {
        // simple search priority (you can improve later)
        if (name != null && !name.isBlank()) {
            return medicineRepository.findByNameContainingIgnoreCase(name, pageable).map(this::toResponse);
        }
        if (brand != null && !brand.isBlank()) {
            return medicineRepository.findByBrandContainingIgnoreCase(brand, pageable).map(this::toResponse);
        }
        if (categoryId != null) {
            return medicineRepository.findByCategory_Id(categoryId, pageable).map(this::toResponse);
        }
        if (status != null && !status.isBlank()) {
            if ("LOW_STOCK".equalsIgnoreCase(status)) {
                return medicineRepository.findByStockQtyBetween(1, 19, pageable).map(this::toResponse);
            }
            return medicineRepository.findByStatus(status, pageable).map(this::toResponse);
        }
        return listAll(pageable);
    }

    @Transactional
    public MedicineResponse update(Long id, MedicineUpdateRequest req) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        medicine.setName(req.getName());
        medicine.setBrand(req.getBrand());
        medicine.setDosage(formatDosage(req.getDosage()));
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
        r.setFinalPrice(m.getFinalPrice());
        r.setStockQty(m.getStockQty());
        r.setExpiryDate(m.getExpiryDate());
        r.setPrescriptionRequired(Boolean.TRUE.equals(m.getPrescriptionRequired()));
        r.setStatus(m.getStatus());
        r.setTypeLabel(m.getTypeLabel());
        r.setCategoryId(m.getCategory().getId());
        r.setCategoryName(m.getCategory().getName());
        return r;
    }

    private String formatDosage(String dosage) {
        if (dosage == null || dosage.isBlank()) {
            return dosage;
        }
        dosage = dosage.trim();
        if (dosage.matches("\\d+(\\.\\d+)?")) {
            return dosage + "mg";
        }
        return dosage;
    }
}