package org.pgno20.medimart.repository;

import org.pgno20.medimart.entity.ProcurementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcurementRequestRepository extends JpaRepository<ProcurementRequest, Long> {
    List<ProcurementRequest> findByStatusOrderByCreatedAtDesc(String status);
}
