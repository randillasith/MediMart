package org.pgno20.medimart.repository;

import org.pgno20.medimart.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findBySku(String sku);

    Page<Medicine> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Medicine> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

    Page<Medicine> findByStatus(String status, Pageable pageable);

    Page<Medicine> findByCategory_Id(Long categoryId, Pageable pageable);

    Page<Medicine> findByStockQtyBetween(Integer min, Integer max, Pageable pageable);

    // --- Storefront query (groups batches together) ---
    @Query("SELECT new org.pgno20.medimart.dto.StorefrontMedicineDTO(m.name, m.brand, m.dosage, MIN(m.price), SUM(CAST(m.stockQty AS long)), m.category.name, MIN(m.expiryDate)) " +
           "FROM Medicine m " +
           "WHERE m.status = 'AVAILABLE' " +
           "GROUP BY m.name, m.brand, m.dosage, m.category.name")
    Page<org.pgno20.medimart.dto.StorefrontMedicineDTO> getStorefrontMedicines(Pageable pageable);

    // --- Stats queries (run entirely in DB, never loads all rows into memory) ---
    @Query("SELECT COUNT(m) FROM Medicine m")
    long countAll();

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.stockQty > 0 AND m.stockQty < 20")
    long countLowStock();

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.stockQty = 0")
    long countOutOfStock();

    @Query("SELECT COALESCE(SUM(m.price * m.stockQty), 0) FROM Medicine m WHERE m.status <> 'DISCONTINUED'")
    BigDecimal sumTotalValue();
}