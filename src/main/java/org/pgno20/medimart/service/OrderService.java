package org.pgno20.medimart.service;

import org.pgno20.medimart.model.Medicine;
import org.pgno20.medimart.model.Order;
import org.pgno20.medimart.model.StockBatch;
import org.pgno20.medimart.repository.MedicineRepository;
import org.pgno20.medimart.repository.OrderRepository;
import org.pgno20.medimart.repository.StockBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles all business logic for customer orders.
 *
 * Stock deduction strategy — FEFO (First Expiry, First Out):
 *   When an order is placed, we consume stock from the batch with the
 *   earliest expiry date first. Once a batch hits 0 it is marked DEPLETED.
 *   After all batches are updated we recalculate medicine.stockQty as the
 *   sum of all remaining ACTIVE batch quantities, then update the medicine
 *   status (AVAILABLE / LOW_STOCK / OUT_OF_STOCK) accordingly.
 *
 * Why inventory wasn't deducting from stock_batches before:
 *   The previous implementation only ran a bulk UPDATE on the medicines table
 *   (medicine.stockQty) using a JPQL query, but never touched the stock_batches
 *   table at all. The storefront reads stock from stock_batches, so the UI
 *   appeared unchanged even after an order. This version deducts directly
 *   from stock_batches using FEFO ordering, then syncs medicine.stockQty.
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private StockBatchRepository stockBatchRepository;

    /**
     * Places an order and deducts inventory in a single DB transaction.
     * If stock deduction fails for any item the whole transaction rolls back.
     */
    @Transactional
    public Order placeOrder(Order order) {
        // 1. Parse ordered items and deduct from stock_batches (FEFO)
        if (order.getMedicineDetails() != null && !order.getMedicineDetails().isBlank()) {
            deductStockFEFO(order.getMedicineDetails());
        }

        // 2. Save the order record
        return orderRepository.save(order);
    }

    /**
     * Parses the medicine details string and applies FEFO deduction
     * against the stock_batches table.
     *
     * Expected format per item: "Medicine Name (xQTY)"
     * e.g. "Paracetamol 500mg (x2), Amoxicillin 250mg (x1)"
     */
    private void deductStockFEFO(String details) {
        // Matches: "anything (xDIGITS)"
        Pattern pattern = Pattern.compile("(.+?)\\s+\\(x(\\d+)\\)");

        for (String part : details.split(",")) {
            Matcher matcher = pattern.matcher(part.trim());
            if (!matcher.find()) continue;

            String medicineName = matcher.group(1).trim();
            int remainingToDeduct = Integer.parseInt(matcher.group(2));

            // Fetch ACTIVE batches for this medicine in FEFO order (earliest expiry first)
            List<StockBatch> batches =
                    stockBatchRepository.findActiveBatchesByMedicineNameFEFO(medicineName);

            for (StockBatch batch : batches) {
                if (remainingToDeduct <= 0) break;

                int available = batch.getQuantity();
                if (available <= 0) {
                    batch.setStatus("DEPLETED");
                    stockBatchRepository.save(batch);
                    continue;
                }

                if (available >= remainingToDeduct) {
                    // This batch covers the rest
                    batch.setQuantity(available - remainingToDeduct);
                    remainingToDeduct = 0;
                } else {
                    // Use all of this batch and continue to the next
                    remainingToDeduct -= available;
                    batch.setQuantity(0);
                }

                // Mark depleted if empty
                if (batch.getQuantity() == 0) {
                    batch.setStatus("DEPLETED");
                }

                stockBatchRepository.save(batch);
            }

            // After batch deduction, sync medicine.stockQty from the sum of active batches
            // and update its status (AVAILABLE / LOW_STOCK / OUT_OF_STOCK)
            syncMedicineStock(medicineName);
        }
    }

    /**
     * Recalculates medicine.stockQty as the sum of all ACTIVE batch quantities
     * for the given medicine name, then updates the medicine status.
     */
    private void syncMedicineStock(String medicineName) {
        List<Medicine> medicines = medicineRepository.findByNameIgnoreCase(medicineName);
        for (Medicine m : medicines) {
            int totalStock = stockBatchRepository.sumActiveQuantityByMedicineId(m.getId());
            m.setStockQty(totalStock);
            m.normalizeStatusFromStock();
            medicineRepository.save(m);
        }
    }

    /** Returns all orders — admin use only. */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Updates the status of an existing order (PENDING → PROCESSING → DELIVERED).
     * Throws IllegalArgumentException (→ 404) if the order is not found.
     */
    @Transactional
    public Order updateOrderStatus(String id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * Cancels and deletes an order by ID.
     * NOTE: Does not restore stock — add reverse-deduction logic here if needed.
     */
    @Transactional
    public void cancelOrder(String id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }
}