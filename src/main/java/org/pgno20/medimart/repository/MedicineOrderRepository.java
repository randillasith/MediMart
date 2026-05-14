package org.pgno20.medimart.repository;

import org.pgno20.medimart.entity.MedicineOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicineOrderRepository extends JpaRepository<MedicineOrder, Long> {

    List<MedicineOrder> findBySupplierId(String supplierId);

    List<MedicineOrder> findByStatus(String status);

    List<MedicineOrder> findByMedicineNameContainingIgnoreCase(String medicineName);

    long countByStatus(String status);
}
