package org.pgno20.medimart.repository;

import org.pgno20.medimart.model.StockBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockBatchRepository extends JpaRepository<StockBatch, Long> {

    // All batches for a medicine, newest first
    List<StockBatch> findByMedicineIdOrderByAddedDateDesc(Long medicineId);

    // Active batches ordered by expiry (FEFO — First Expiry, First Out)
    List<StockBatch> findByMedicineIdAndStatusOrderByExpiryDateAsc(Long medicineId, String status);

    // Total active stock for a medicine (runs in DB, never loads entities)
    @Query("SELECT COALESCE(SUM(sb.quantity), 0) FROM StockBatch sb WHERE sb.medicine.id = :medicineId AND sb.status = 'ACTIVE'")
    int sumActiveQuantityByMedicineId(@Param("medicineId") Long medicineId);

    // Earliest expiry among active batches
    @Query("SELECT MIN(sb.expiryDate) FROM StockBatch sb WHERE sb.medicine.id = :medicineId AND sb.status = 'ACTIVE' AND sb.expiryDate IS NOT NULL")
    LocalDate findEarliestExpiryByMedicineId(@Param("medicineId") Long medicineId);

    // Count of active batches for a medicine
    @Query("SELECT COUNT(sb) FROM StockBatch sb WHERE sb.medicine.id = :medicineId AND sb.status = 'ACTIVE'")
    long countActiveBatchesByMedicineId(@Param("medicineId") Long medicineId);

    // Total count of batches for a medicine (all statuses)
    long countByMedicineId(Long medicineId);
}
