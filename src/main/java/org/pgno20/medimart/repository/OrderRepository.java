package org.pgno20.medimart.repository;

import org.pgno20.medimart.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal calculateTotalRevenue();

    long countByStatus(String status);

    List<Order> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED' AND o.prescriptionSubmitted = :isPrescription")
    BigDecimal calculateTotalRevenueByPrescription(boolean isPrescription);

    @Query("SELECT o FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate")
    List<Order> findDeliveredOrdersSince(java.time.LocalDateTime startDate);
}