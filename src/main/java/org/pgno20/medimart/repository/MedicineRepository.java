package org.pgno20.medimart.repository;

import org.pgno20.medimart.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findBySku(String sku);

    List<Medicine> findByNameContainingIgnoreCase(String name);

    List<Medicine> findByBrandContainingIgnoreCase(String brand);

    List<Medicine> findByStatus(String status);

    List<Medicine> findByCategory_Id(Long categoryId);
}