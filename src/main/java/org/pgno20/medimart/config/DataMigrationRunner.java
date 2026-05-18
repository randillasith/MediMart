package org.pgno20.medimart.config;

import org.pgno20.medimart.model.Medicine;
import org.pgno20.medimart.model.StockBatch;
import org.pgno20.medimart.repository.MedicineRepository;
import org.pgno20.medimart.repository.StockBatchRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One-time data migration: creates an initial StockBatch for every existing
 * Medicine that has stock but no batches yet.
 * Runs automatically on application startup.
 * Safe to re-run — skips if batches already exist.
 */
@Component
public class DataMigrationRunner implements CommandLineRunner {

    private final MedicineRepository medicineRepository;
    private final StockBatchRepository stockBatchRepository;

    public DataMigrationRunner(MedicineRepository medicineRepository, StockBatchRepository stockBatchRepository) {
        this.medicineRepository = medicineRepository;
        this.stockBatchRepository = stockBatchRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Only migrate if no batches exist yet (first-time migration)
        if (stockBatchRepository.count() > 0) {
            System.out.println("[DataMigration] Stock batches already exist. Skipping migration.");
            return;
        }

        List<Medicine> medicines = medicineRepository.findAll();
        int migratedCount = 0;

        for (Medicine m : medicines) {
            if (m.getStockQty() != null && m.getStockQty() > 0 && !"DISCONTINUED".equals(m.getStatus())) {
                StockBatch batch = new StockBatch();
                batch.setMedicine(m);
                // Temporary batch number
                batch.setBatchNumber("INIT-" + String.format("%03d", m.getId()));
                batch.setQuantity(m.getStockQty());
                batch.setExpiryDate(m.getExpiryDate());
                batch.setPurchasePrice(m.getPrice()); // Use current price as purchase price
                batch.setAddedDate(LocalDateTime.now());
                batch.setStatus("ACTIVE");

                StockBatch saved = stockBatchRepository.save(batch);

                // Update to readable batch number
                saved.setBatchNumber(String.format("BATCH-%04d", saved.getId()));
                stockBatchRepository.save(saved);

                migratedCount++;
            }
        }

        System.out.println("[DataMigration] Created " + migratedCount + " initial stock batches for existing medicines.");
    }
}
