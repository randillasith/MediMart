package org.pgno20.medimart.service;

import org.pgno20.medimart.dto.StockBatchRequest;
import org.pgno20.medimart.dto.StockBatchResponse;
import org.pgno20.medimart.model.Medicine;
import org.pgno20.medimart.model.StockBatch;
import org.pgno20.medimart.repository.MedicineRepository;
import org.pgno20.medimart.repository.StockBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StockBatchService {

    private final StockBatchRepository stockBatchRepository;
    private final MedicineRepository medicineRepository;

    public StockBatchService(StockBatchRepository stockBatchRepository, MedicineRepository medicineRepository) {
        this.stockBatchRepository = stockBatchRepository;
        this.medicineRepository = medicineRepository;
    }

    /**
     * Add a new stock batch (restock) for an existing medicine.
     * Recalculates the medicine's cached stockQty and expiryDate.
     */
    @Transactional
    public StockBatchResponse addBatch(Long medicineId, StockBatchRequest req) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));

        StockBatch batch = new StockBatch();
        batch.setMedicine(medicine);
        // Temporary batch number — will be updated after save to get the ID
        batch.setBatchNumber("TEMP-" + UUID.randomUUID().toString().substring(0, 8));
        batch.setQuantity(req.getQuantity());
        batch.setExpiryDate(req.getExpiryDate());
        batch.setPurchasePrice(req.getPurchasePrice() != null ? req.getPurchasePrice() : medicine.getPrice());
        batch.setStatus("ACTIVE");

        // Check if batch is already expired
        if (batch.isExpired()) {
            batch.setStatus("EXPIRED");
        }



        StockBatch saved = stockBatchRepository.save(batch);

        // Update batch number to readable format: BATCH-0001
        saved.setBatchNumber(String.format("BATCH-%04d", saved.getId()));
        saved = stockBatchRepository.save(saved);

        // Recalculate medicine's cached stock fields
        recalculateMedicineStock(medicine);

        return toResponse(saved);
    }

    /**
     * Create an initial batch when a new medicine is created.
     * Called internally by MedicineService.
     */
    @Transactional
    public void createInitialBatch(Medicine medicine, Integer quantity, LocalDate expiryDate) {
        if (quantity == null || quantity <= 0) return;

        StockBatch batch = new StockBatch();
        batch.setMedicine(medicine);
        batch.setBatchNumber("TEMP-" + UUID.randomUUID().toString().substring(0, 8));
        batch.setQuantity(quantity);
        batch.setExpiryDate(expiryDate);
        batch.setPurchasePrice(medicine.getPrice());
        batch.setStatus("ACTIVE");

        if (batch.isExpired()) {
            batch.setStatus("EXPIRED");
        }

        StockBatch saved = stockBatchRepository.save(batch);
        saved.setBatchNumber(String.format("BATCH-%04d", saved.getId()));
        stockBatchRepository.save(saved);
    }

    /**
     * Get all batches for a medicine, newest first.
     */
    public List<StockBatchResponse> getBatchesForMedicine(Long medicineId) {
        if (!medicineRepository.existsById(medicineId)) {
            throw new IllegalArgumentException("Medicine not found");
        }

        return stockBatchRepository.findByMedicineIdOrderByAddedDateDesc(medicineId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update a batch (adjust quantity or expiry).
     */
    @Transactional
    public StockBatchResponse updateBatch(Long medicineId, Long batchId, StockBatchRequest req) {
        StockBatch batch = stockBatchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));

        if (!batch.getMedicine().getId().equals(medicineId)) {
            throw new IllegalArgumentException("Batch does not belong to this medicine");
        }

        batch.setQuantity(req.getQuantity());
        if (req.getExpiryDate() != null) {
            batch.setExpiryDate(req.getExpiryDate());
        }
        if (req.getPurchasePrice() != null) {
            batch.setPurchasePrice(req.getPurchasePrice());
        }

        // Update status based on quantity and expiry
        if (batch.getQuantity() <= 0) {
            batch.setStatus("DEPLETED");
        } else if (batch.isExpired()) {
            batch.setStatus("EXPIRED");
        } else {
            batch.setStatus("ACTIVE");
        }

        StockBatch saved = stockBatchRepository.save(batch);

        // Recalculate parent medicine's cached stock
        recalculateMedicineStock(batch.getMedicine());

        return toResponse(saved);
    }

    /**
     * Get count of active batches for a medicine.
     */
    public long getActiveBatchCount(Long medicineId) {
        return stockBatchRepository.countActiveBatchesByMedicineId(medicineId);
    }

    /**
     * Recalculate the medicine's cached stockQty and expiryDate from its batches.
     * Also updates the medicine's status based on the new total.
     */
    @Transactional
    public void recalculateMedicineStock(Medicine medicine) {
        int totalStock = stockBatchRepository.sumActiveQuantityByMedicineId(medicine.getId());
        LocalDate earliestExpiry = stockBatchRepository.findEarliestExpiryByMedicineId(medicine.getId());

        medicine.setStockQty(totalStock);
        medicine.setExpiryDate(earliestExpiry);

        // FEFO Pricing: Set the product's base price to the price of the oldest active batch (next to be sold)
        List<StockBatch> activeBatches = stockBatchRepository.findByMedicineIdAndStatusOrderByExpiryDateAsc(medicine.getId(), "ACTIVE");
        if (!activeBatches.isEmpty()) {
            java.math.BigDecimal activePrice = activeBatches.get(0).getPurchasePrice();
            if (activePrice != null) {
                medicine.setPrice(activePrice);
            }
        }

        // Recalculate status
        medicine.normalizeStatusFromStock();

        medicineRepository.save(medicine);
    }

    private StockBatchResponse toResponse(StockBatch batch) {
        StockBatchResponse r = new StockBatchResponse();
        r.setId(batch.getId());
        r.setBatchNumber(batch.getBatchNumber());
        r.setQuantity(batch.getQuantity());
        r.setPurchasePrice(batch.getPurchasePrice());
        r.setExpiryDate(batch.getExpiryDate());
        r.setAddedDate(batch.getAddedDate());
        r.setStatus(batch.getStatus());
        return r;
    }
}
