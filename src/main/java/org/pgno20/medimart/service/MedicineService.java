package org.pgno20.medimart.service;

import org.pgno20.medimart.dto.MedicineCreateRequest;
import org.pgno20.medimart.dto.MedicineResponse;
import org.pgno20.medimart.dto.MedicineUpdateRequest;
import org.pgno20.medimart.model.*;
import org.pgno20.medimart.repository.CategoryRepository;
import org.pgno20.medimart.repository.MedicineRepository;
import org.pgno20.medimart.repository.StockBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.UUID;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;
    private final StockBatchRepository stockBatchRepository;
    private final StockBatchService stockBatchService;
    private final NotificationService notificationService;

    public MedicineService(MedicineRepository medicineRepository, CategoryRepository categoryRepository,
                           StockBatchRepository stockBatchRepository, StockBatchService stockBatchService,
                           NotificationService notificationService) {
        this.medicineRepository = medicineRepository;
        this.categoryRepository = categoryRepository;
        this.stockBatchRepository = stockBatchRepository;
        this.stockBatchService = stockBatchService;
        this.notificationService = notificationService;
    }

    @Transactional
    public MedicineResponse create(MedicineCreateRequest req) {


        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        String formattedDosage = formatDosage(req.getDosage());

        // Check for exact duplicates
        java.util.Optional<Medicine> duplicate = medicineRepository.findExactDuplicate(
                req.getName(), req.getBrand(), formattedDosage, req.getFormType()
        );

        if (duplicate.isPresent()) {
            throw new IllegalArgumentException("Duplicate item found! This medicine already exists. Please add your new stock to the existing item (" + duplicate.get().getSku() + ") instead of creating a new one.");
        }

        Medicine medicine = buildMedicineByType(req.getType());
        // Shorten the temp string so it fits in the 40-char limit
        medicine.setSku("TEMP-" + java.util.UUID.randomUUID().toString().substring(0, 8));
        medicine.setName(req.getName());
        medicine.setBrand(req.getBrand());
        medicine.setFormType(req.getFormType());
        medicine.setDosage(formattedDosage);
        medicine.setPrice(req.getPrice());
        medicine.setStockQty(req.getStockQty());
        medicine.setExpiryDate(req.getExpiryDate());
        medicine.setCategory(category);

        // Business rules
        medicine.normalizeStatusFromStock();
        
        assignDefaultImage(medicine);

        // First save to generate the ID
        Medicine saved = medicineRepository.save(medicine);
        
        // Trigger notification if low stock on creation
        if (saved.getStockQty() != null && saved.getStockQty() <= 100) {
            notificationService.triggerLowStockAlert(saved.getName(), saved.getId());
        }
        
        // Update SKU with the MED001 format based on the generated ID
        saved.setSku(String.format("MED%03d", saved.getId()));
        saved = medicineRepository.save(saved);

        // Create the initial stock batch from the provided stockQty and expiryDate
        stockBatchService.createInitialBatch(saved, req.getStockQty(), req.getExpiryDate(), req.getSupplierId());
        
        return toResponse(saved);
    }

    public Page<MedicineResponse> listAll(Pageable pageable) {
        return medicineRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<org.pgno20.medimart.dto.StorefrontMedicineDTO> getStorefrontMedicines(Pageable pageable) {
        return medicineRepository.getStorefrontMedicines(pageable).map(this::applyFinalPrice);
    }

    public Page<org.pgno20.medimart.dto.StorefrontMedicineDTO> searchStorefrontMedicines(String search, Pageable pageable) {
        return medicineRepository.searchStorefrontMedicines(search, pageable).map(this::applyFinalPrice);
    }

    /**
     * Applies OOP polymorphism pricing to storefront DTOs.
     * OTC: base price + 10% tax
     * Prescription: base price + $5.00 dispensing fee
     */
    private org.pgno20.medimart.dto.StorefrontMedicineDTO applyFinalPrice(org.pgno20.medimart.dto.StorefrontMedicineDTO dto) {
        if (dto.getMinPrice() != null) {
            if (Boolean.TRUE.equals(dto.getPrescriptionRequired())) {
                dto.setFinalPrice(dto.getMinPrice().add(new java.math.BigDecimal("5.00")));
                dto.setTypeLabel("Prescription Required");
            } else {
                dto.setFinalPrice(dto.getMinPrice().multiply(new java.math.BigDecimal("1.10"))
                        .setScale(2, java.math.RoundingMode.HALF_UP));
                dto.setTypeLabel("OTC");
            }
        } else {
            dto.setFinalPrice(java.math.BigDecimal.ZERO);
            dto.setTypeLabel("OTC");
        }
        return dto;
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
        String searchName = (name != null && !name.isBlank()) ? name : null;
        String searchBrand = (brand != null && !brand.isBlank()) ? brand : null;
        String searchStatus = (status != null && !status.isBlank()) ? status : null;
        
        return medicineRepository.searchMedicines(searchName, searchBrand, categoryId, searchStatus, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public MedicineResponse update(Long id, MedicineUpdateRequest req) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        medicine.setName(req.getName());
        medicine.setBrand(req.getBrand());
        medicine.setFormType(req.getFormType());
        medicine.setDosage(formatDosage(req.getDosage()));
        medicine.setPrice(req.getPrice());
        medicine.setStockQty(req.getStockQty());
        medicine.setExpiryDate(req.getExpiryDate());
        medicine.setCategory(category);

        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            medicine.setStatus(req.getStatus());
        }

        // Business rules
        if (req.getStatus() == null || req.getStatus().isBlank()) {
            // Only auto-normalize if the admin didn't explicitly set a status
            medicine.normalizeStatusFromStock();
        }

        Medicine updated = medicineRepository.save(medicine);
        
        // Check for low stock alert
        if (updated.getStockQty() != null && updated.getStockQty() <= 100 && !updated.getStatus().equals("DISCONTINUED")) {
            notificationService.triggerLowStockAlert(updated.getName(), updated.getId());
        } else {
            notificationService.clearLowStockAlert(updated.getId());
        }

        return toResponse(updated);
    }

    @Transactional
    public void discontinue(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));
        medicine.setStatus("DISCONTINUED");
        medicineRepository.save(medicine);
    }

    @Transactional
    public String uploadImage(Long id, MultipartFile file) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = medicine.getSku() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            Path uploadPath = Paths.get("uploads");
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            medicine.setImageUrl(filename);
            medicineRepository.save(medicine);

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image file", e);
        }
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
        r.setFormType(m.getFormType());
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
        r.setImageUrl(m.getImageUrl());
        r.setBatchCount(stockBatchRepository.countActiveBatchesByMedicineId(m.getId()));
        return r;
    }

    private void assignDefaultImage(Medicine medicine) {
        String defaultName = "other.png";
        if (medicine.getFormType() != null) {
            switch(medicine.getFormType().toUpperCase()) {
                case "TABLET": defaultName = "Tablet_Capsule_Pill.png"; break;
                case "SYRUP": defaultName = "Syrup_Liquid.png"; break;
                case "CREAM": defaultName = "cream.png"; break;
            }
        }
        try {
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String newFilename = medicine.getSku() + "_default_" + defaultName;
            Path dest = uploadPath.resolve(newFilename);
            if (!Files.exists(dest)) {
                try (java.io.InputStream in = getClass().getResourceAsStream("/images/" + defaultName)) {
                    if (in != null) Files.copy(in, dest);
                }
            }
            medicine.setImageUrl(newFilename);
        } catch (Exception e) {
            e.printStackTrace();
        }
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