package org.pgno20.medimart.repository;

import org.pgno20.medimart.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findBySku(String sku);

    Page<Medicine> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Medicine> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

    Page<Medicine> findByStatus(String status, Pageable pageable);
    Page<Medicine> findByCategory_Id(Long categoryId, Pageable pageable);

    Page<Medicine> findByStockQtyBetween(Integer min, Integer max, Pageable pageable);
}