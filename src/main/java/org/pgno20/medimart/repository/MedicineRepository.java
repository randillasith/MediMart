package org.pgno20.medimart.repository;

import org.pgno20.medimart.dto.StorefrontMedicineDTO;
import org.pgno20.medimart.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findBySku(String sku);

    @Query("SELECT m FROM Medicine m WHERE " +
           "LOWER(m.name) = LOWER(:name) AND " +
           "((:brand IS NULL AND m.brand IS NULL) OR LOWER(m.brand) = LOWER(:brand)) AND " +
           "((:dosage IS NULL AND m.dosage IS NULL) OR LOWER(m.dosage) = LOWER(:dosage)) AND " +
           "((:formType IS NULL AND m.formType IS NULL) OR LOWER(m.formType) = LOWER(:formType))")
    Optional<Medicine> findExactDuplicate(
            @Param("name") String name, 
            @Param("brand") String brand, 
            @Param("dosage") String dosage, 
            @Param("formType") String formType);

    Page<Medicine> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Medicine> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

    Page<Medicine> findByStatus(String status, Pageable pageable);

    Page<Medicine> findByCategory_Id(Long categoryId, Pageable pageable);

    Page<Medicine> findByStockQtyBetween(Integer min, Integer max, Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE " +
           "(:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:brand IS NULL OR LOWER(m.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) AND " +
           "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
           "(:status IS NULL OR " +
           "  (:status = 'LOW_STOCK' AND m.stockQty > 0 AND m.stockQty <= 100 AND m.status <> 'DISCONTINUED') OR " +
           "  (:status <> 'LOW_STOCK' AND m.status = :status)" +
           ")")
    Page<Medicine> searchMedicines(
            @Param("name") String name, 
            @Param("brand") String brand, 
            @Param("categoryId") Long categoryId, 
            @Param("status") String status, 
            Pageable pageable);

    // --- Storefront queries (groups batches together for customer view) ---

    @Query("SELECT new org.pgno20.medimart.dto.StorefrontMedicineDTO(m.name, m.brand, m.dosage, MIN(m.price), SUM(CAST(m.stockQty AS long)), m.category.name, MIN(m.expiryDate), m.prescriptionRequired, m.imageUrl, m.formType) " +
           "FROM Medicine m " +
           "WHERE m.status <> 'DISCONTINUED' " +
           "GROUP BY m.name, m.brand, m.dosage, m.category.name, m.prescriptionRequired, m.imageUrl, m.formType")
    Page<StorefrontMedicineDTO> getStorefrontMedicines(Pageable pageable);

    @Query("SELECT new org.pgno20.medimart.dto.StorefrontMedicineDTO(m.name, m.brand, m.dosage, MIN(m.price), SUM(CAST(m.stockQty AS long)), m.category.name, MIN(m.expiryDate), m.prescriptionRequired, m.imageUrl, m.formType) " +
           "FROM Medicine m " +
           "WHERE m.status <> 'DISCONTINUED' AND LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "GROUP BY m.name, m.brand, m.dosage, m.category.name, m.prescriptionRequired, m.imageUrl, m.formType")
    Page<StorefrontMedicineDTO> searchStorefrontMedicines(@Param("search") String search, Pageable pageable);

    // --- Stats queries (run entirely in DB, never loads all rows into memory) ---
    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.status <> 'DISCONTINUED'")
    long countAll();

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.stockQty > 0 AND m.stockQty <= 100 AND m.status <> 'DISCONTINUED'")
    long countLowStock();

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.stockQty = 0 AND m.status <> 'DISCONTINUED'")
    long countOutOfStock();

    @Query("SELECT COALESCE(SUM(m.price * m.stockQty), 0) FROM Medicine m WHERE m.status <> 'DISCONTINUED'")
    BigDecimal sumTotalValue();

    /** Used by OrderService after FEFO deduction to sync medicine.stockQty by name. */
    java.util.List<Medicine> findByNameIgnoreCase(String name);
}
