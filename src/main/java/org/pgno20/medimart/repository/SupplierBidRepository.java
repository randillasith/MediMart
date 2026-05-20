package org.pgno20.medimart.repository;

import org.pgno20.medimart.entity.SupplierBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierBidRepository extends JpaRepository<SupplierBid, Long> {
    List<SupplierBid> findByProcurementRequestId(Long requestId);
    List<SupplierBid> findBySupplierId(String supplierId);
}
