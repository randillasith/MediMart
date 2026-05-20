package org.pgno20.medimart.service;

import org.pgno20.medimart.model.Medicine;
import org.pgno20.medimart.model.Order;
import org.pgno20.medimart.model.StockBatch;
import org.pgno20.medimart.model.Prescription;
import org.pgno20.medimart.repository.MedicineRepository;
import org.pgno20.medimart.repository.OrderRepository;
import org.pgno20.medimart.repository.StockBatchRepository;
import org.pgno20.medimart.service.SystemSettingsService;
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

    @Autowired
    private SystemSettingsService settingsService;

    @Autowired
    private org.pgno20.medimart.repository.PrescriptionRepository prescriptionRepository;

    /**
     * Places an order and deducts inventory in a single DB transaction.
     * If stock deduction fails for any item the whole transaction rolls back.
     */
    @Transactional
    public Order placeOrder(Order order) {
        // 1. Deduct stock for each item using medicineId (FEFO) and populate snapshot data
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (org.pgno20.medimart.model.OrderItem item : order.getItems()) {
                Medicine m = null;
                if (item.getMedicineId() != null) {
                    m = medicineRepository.findById(item.getMedicineId()).orElse(null);
                }
                
                // Fallback for old carts where medicineId wasn't stored
                if (m == null && item.getMedicineName() != null) {
                    List<Medicine> meds = medicineRepository.findByNameIgnoreCase(item.getMedicineName());
                    if (!meds.isEmpty()) {
                        m = meds.get(0);
                        item.setMedicineId(m.getId()); // Update the ID for the database
                    }
                }

                if (m == null) {
                    throw new IllegalArgumentException("Medicine not found: " + 
                        (item.getMedicineId() != null ? item.getMedicineId() : item.getMedicineName()));
                }

                // ── Fix #2: Validate stock BEFORE deducting ──────────────────
                if (m.getStockQty() < item.getQuantity()) {
                    throw new IllegalArgumentException(
                        "Insufficient stock for '" + m.getName() + "'. " +
                        "Requested: " + item.getQuantity() + ", Available: " + m.getStockQty()
                    );
                }
                
                // Set snapshot values
                item.setMedicineName(m.getName());
                
                java.math.BigDecimal finalUnitPrice = m.getPrice();
                if (finalUnitPrice != null) {
                    var settings = settingsService.getSettings();
                    if (Boolean.TRUE.equals(m.getPrescriptionRequired())) {
                        // Prescription medicine: dynamic dispensing fee is flat per unique line item/order,
                        // so B2C order items' unit price is saved as m.getPrice() (excluding dispensing fee).
                        finalUnitPrice = m.getPrice();
                    } else {
                        // OTC: apply dynamic otcTaxRate
                        java.math.BigDecimal taxRate = settings.getOtcTaxRate();
                        java.math.BigDecimal multiplier = java.math.BigDecimal.ONE
                            .add(taxRate.divide(new java.math.BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP));
                        finalUnitPrice = m.getPrice().multiply(multiplier)
                                .setScale(2, java.math.RoundingMode.HALF_UP);
                    }
                } else {
                    finalUnitPrice = java.math.BigDecimal.ZERO;
                }
                item.setUnitPrice(finalUnitPrice);
                item.setOrder(order);

                deductStockItem(item.getMedicineName(), item.getQuantity());
            }
        } else if (order.getMedicineDetails() != null && !order.getMedicineDetails().isBlank()) {
             // Fallback for backwards compatibility if old payload is sent
             deductStockFEFO(order.getMedicineDetails());
        }

        // Auto-build medicineDetails if not present or blank
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < order.getItems().size(); i++) {
                org.pgno20.medimart.model.OrderItem item = order.getItems().get(i);
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(item.getMedicineName()).append(" (x").append(item.getQuantity()).append(")");
            }
            order.setMedicineDetails(sb.toString());
        }

        // 2. Save the order record (cascades to order_items)
        return orderRepository.save(order);
    }

    /**
     * Deducts stock for a specific medicine name using FEFO ordering.
     */
    private void deductStockItem(String medicineName, int remainingToDeduct) {
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

        // After batch deduction, sync medicine.stockQty
        syncMedicineStock(medicineName);
    }

    /**
     * Recalculates medicine.stockQty by ID and updates its status.
     */
    private void syncMedicineStockById(Long medicineId) {
        Medicine m = medicineRepository.findById(medicineId).orElse(null);
        if (m != null) {
            int totalStock = stockBatchRepository.sumActiveQuantityByMedicineId(medicineId);
            m.setStockQty(totalStock);
            m.normalizeStatusFromStock();
            medicineRepository.save(m);
        }
    }


    /**
     * Parses the medicine details string and applies FEFO deduction (Legacy fallback).
     */
    private void deductStockFEFO(String details) {
        Pattern pattern = Pattern.compile("(.+?)\\s+\\(x(\\d+)\\)");
        for (String part : details.split(",")) {
            Matcher matcher = pattern.matcher(part.trim());
            if (!matcher.find()) continue;

            String medicineName = matcher.group(1).trim();
            int remainingToDeduct = Integer.parseInt(matcher.group(2));
            List<StockBatch> batches = stockBatchRepository.findActiveBatchesByMedicineNameFEFO(medicineName);
            // ... (deduction logic for string parsing - keeping it simple for fallback)
            for(StockBatch batch : batches) {
               if(remainingToDeduct <= 0) break;
               int available = batch.getQuantity();
               if(available <= 0) { batch.setStatus("DEPLETED"); stockBatchRepository.save(batch); continue; }
               if(available >= remainingToDeduct) { batch.setQuantity(available - remainingToDeduct); remainingToDeduct = 0; }
               else { remainingToDeduct -= available; batch.setQuantity(0); }
               if(batch.getQuantity() == 0) batch.setStatus("DEPLETED");
               stockBatchRepository.save(batch);
            }
            syncMedicineStock(medicineName);
        }
    }

    private void syncMedicineStock(String medicineName) {
        List<Medicine> medicines = medicineRepository.findByNameIgnoreCase(medicineName);
        for (Medicine m : medicines) {
            syncMedicineStockById(m.getId());
        }
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order updateOrderStatus(String id, String newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        String oldStatus = order.getStatus();
        
        // Clinical Validation: If transitioning to active/fulfilled, ensure any required prescription is APPROVED
        if ("PROCESSING".equals(newStatus) || "SHIPPED".equals(newStatus) || "DELIVERED".equals(newStatus)) {
            if (requiresPrescription(order)) {
                String rxId = order.getPrescriptionId();
                if (rxId == null || rxId.isBlank()) {
                    // Fallback to searching by customer email
                    String email = order.getCustomerEmail();
                    if (email == null || email.isBlank()) {
                        throw new IllegalArgumentException("Cannot process order: A prescription is required but no prescription ID or customer email was provided.");
                    }
                    List<Prescription> prescriptions = prescriptionRepository.findBySubmittedByEmailIgnoreCase(email);
                    boolean hasApproved = false;
                    for (Prescription p : prescriptions) {
                        if ("APPROVED".equalsIgnoreCase(p.getStatus()) && !p.isExpired()) {
                            hasApproved = true;
                            break;
                        }
                    }
                    if (!hasApproved) {
                        throw new IllegalArgumentException("Cannot process order: The prescription for this order has not been approved or has expired.");
                    }
                } else {
                    // Find specific prescription by ID
                    Prescription p = prescriptionRepository.findByPrescriptionId(rxId).orElse(null);
                    if (p == null) {
                        throw new IllegalArgumentException("Cannot process order: Associated prescription " + rxId + " was not found.");
                    }
                    if (!"APPROVED".equalsIgnoreCase(p.getStatus())) {
                        throw new IllegalArgumentException("Cannot process order: Associated prescription " + rxId + " is not approved (status: " + p.getStatus() + ").");
                    }
                    if (p.isExpired()) {
                        throw new IllegalArgumentException("Cannot process order: Associated prescription " + rxId + " has expired.");
                    }
                }
            }
        }

        order.setStatus(newStatus);
        
        // If status is changed to CANCELLED from something else, restore stock
        if ("CANCELLED".equals(newStatus) && !"CANCELLED".equals(oldStatus)) {
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (org.pgno20.medimart.model.OrderItem item : order.getItems()) {
                    restoreStockItem(item.getMedicineName(), item.getQuantity());
                }
            } else if (order.getMedicineDetails() != null && !order.getMedicineDetails().isBlank()) {
                restoreStockFEFO(order.getMedicineDetails());
            }
        }
        
        return orderRepository.save(order);
    }

    /**
     * Checks if any item in the order requires a prescription.
     */
    public boolean requiresPrescription(Order order) {
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (org.pgno20.medimart.model.OrderItem item : order.getItems()) {
                if (item.getMedicineId() != null) {
                    Medicine m = medicineRepository.findById(item.getMedicineId()).orElse(null);
                    if (m != null && Boolean.TRUE.equals(m.getPrescriptionRequired())) {
                        return true;
                    }
                } else if (item.getMedicineName() != null) {
                    List<Medicine> meds = medicineRepository.findByNameIgnoreCase(item.getMedicineName());
                    if (!meds.isEmpty() && Boolean.TRUE.equals(meds.get(0).getPrescriptionRequired())) {
                        return true;
                    }
                }
            }
        } else {
            // Fallback for string-based or legacy orders
            return order.isPrescriptionSubmitted();
        }
        return false;
    }

    /**
     * Cancels and deletes an order by ID.
     * Before deleting, restores the ordered quantities back to stock batches.
     */
    @Transactional
    public void cancelOrder(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        // Restore stock before deleting if it hasn't been cancelled yet
        if (!"CANCELLED".equals(order.getStatus())) {
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (org.pgno20.medimart.model.OrderItem item : order.getItems()) {
                    restoreStockItem(item.getMedicineName(), item.getQuantity());
                }
            } else if (order.getMedicineDetails() != null && !order.getMedicineDetails().isBlank()) {
                // Legacy fallback
                restoreStockFEFO(order.getMedicineDetails());
            }
        }

        orderRepository.deleteById(id);
    }

    /**
     * Restores stock for a specific medicine. Uses name to handle grouped storefront products.
     */
    private void restoreStockItem(String medicineName, int qtyToRestore) {
        // Find the most recently added batch for this medicine name
        List<StockBatch> batches = stockBatchRepository.findAllBatchesByMedicineNameLatestFirst(medicineName);
        if (!batches.isEmpty()) {
            StockBatch batch = batches.get(0);
            batch.setQuantity(batch.getQuantity() + qtyToRestore);
            if ("DEPLETED".equals(batch.getStatus())) {
                batch.setStatus("ACTIVE");
            }
            stockBatchRepository.save(batch);
            syncMedicineStock(medicineName);
        }
    }

    /**
     * Legacy string-based restore.
     */
    private void restoreStockFEFO(String details) {
        Pattern pattern = Pattern.compile("(.+?)\\s+\\(x(\\d+)\\)");
        for (String part : details.split(",")) {
            Matcher matcher = pattern.matcher(part.trim());
            if (!matcher.find()) continue;
            String medicineName = matcher.group(1).trim();
            int qtyToRestore = Integer.parseInt(matcher.group(2));
            List<StockBatch> batches = stockBatchRepository.findAllBatchesByMedicineNameLatestFirst(medicineName);
            if (!batches.isEmpty()) {
                StockBatch batch = batches.get(0);
                batch.setQuantity(batch.getQuantity() + qtyToRestore);
                if ("DEPLETED".equals(batch.getStatus())) batch.setStatus("ACTIVE");
                stockBatchRepository.save(batch);
                syncMedicineStock(medicineName);
            }
        }
    }
}