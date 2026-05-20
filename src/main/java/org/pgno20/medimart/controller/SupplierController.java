package org.pgno20.medimart.controller;

import org.pgno20.medimart.dto.SupplierResponse;
import org.pgno20.medimart.entity.Supplier;
import org.pgno20.medimart.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import org.pgno20.medimart.entity.MedicineOrder;
import org.pgno20.medimart.repository.MedicineOrderRepository;
import org.pgno20.medimart.repository.SupplierRepository;
import org.pgno20.medimart.repository.MedicineRepository;
import org.pgno20.medimart.entity.SupplierBid;
import org.pgno20.medimart.repository.SupplierBidRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final SupplierRepository supplierRepository;
    private final MedicineOrderRepository medicineOrderRepository;
    private final MedicineRepository medicineRepository;
    private final SupplierBidRepository supplierBidRepository;

    public SupplierController(SupplierService supplierService, SupplierRepository supplierRepository, MedicineOrderRepository medicineOrderRepository, MedicineRepository medicineRepository, SupplierBidRepository supplierBidRepository) {
        this.supplierService = supplierService;
        this.supplierRepository = supplierRepository;
        this.medicineOrderRepository = medicineOrderRepository;
        this.medicineRepository = medicineRepository;
        this.supplierBidRepository = supplierBidRepository;
    }

    // Create
    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody Supplier supplier) {
        SupplierResponse created = supplierService.createSupplier(supplier);
        URI location = URI.create("/api/suppliers/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    // Read all
    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAll() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    // Read by id
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(supplierService.getSupplierResponseById(id));
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(@PathVariable String id, @Valid @RequestBody Supplier supplier) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, supplier));
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    // Search by name
    @GetMapping("/search/name")
    public ResponseEntity<List<SupplierResponse>> searchByName(@RequestParam String q) {
        return ResponseEntity.ok(supplierService.searchByName(q));
    }

    // Search by medicine
    @GetMapping("/search/medicine")
    public ResponseEntity<List<SupplierResponse>> searchByMedicine(@RequestParam String q) {
        return ResponseEntity.ok(supplierService.searchByMedicineKeyword(q));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
                return ResponseEntity.status(401).build();
            }

            String email = auth.getName();
            String normalizedEmail = email != null ? email.trim() : "";
            Supplier supplier = supplierRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> {
                    Supplier newSupplier = new Supplier();
                    newSupplier.setId("SUP" + (System.currentTimeMillis() % 100000));
                    newSupplier.setName("Supplier " + normalizedEmail.split("@")[0]);
                    newSupplier.setEmail(normalizedEmail);
                    newSupplier.setType("WHOLESALER");
                    newSupplier.setContact("000000000");
                    return supplierRepository.save(newSupplier);
                });

            List<Map<String, Object>> history = new ArrayList<>();

            // 1. Add non-accepted bids (PENDING or REJECTED)
            List<SupplierBid> bids = supplierBidRepository.findBySupplierId(supplier.getId());
            for (SupplierBid bid : bids) {
                if (!"ACCEPTED".equals(bid.getStatus())) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderId", bid.getId());
                    map.put("type", "BID");
                    map.put("orderDate", bid.getCreatedAt() != null ? bid.getCreatedAt().toLocalDate().toString() : "");
                    
                    String medName = "Unknown";
                    int qty = 0;
                    if (bid.getProcurementRequest() != null) {
                        medName = bid.getProcurementRequest().getMedicineName() != null ? bid.getProcurementRequest().getMedicineName() : "Unknown";
                        qty = bid.getProcurementRequest().getQuantityRequired() != null ? bid.getProcurementRequest().getQuantityRequired() : 0;
                    }
                    double price = bid.getPrice() != null ? bid.getPrice() : 0.0;
                    
                    map.put("medicineName", medName);
                    map.put("quantity", qty);
                    map.put("unitPrice", price);
                    map.put("totalPrice", price * qty);
                    map.put("status", bid.getStatus());
                    history.add(map);
                }
            }

            // 2. Add accepted orders (which are actual MedicineOrders)
            List<MedicineOrder> orders = medicineOrderRepository.findBySupplierId(supplier.getId());
            for (MedicineOrder order : orders) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", order.getOrderId());
                map.put("type", "ORDER");
                map.put("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : "");
                map.put("medicineName", order.getMedicineName() != null ? order.getMedicineName() : "Unknown");
                map.put("quantity", order.getQuantity() != null ? order.getQuantity() : 0);
                map.put("unitPrice", order.getUnitPrice() != null ? order.getUnitPrice() : 0.0);
                map.put("totalPrice", order.getTotalPrice() != null ? order.getTotalPrice() : 0.0);
                map.put("status", order.getStatus());
                history.add(map);
            }

            // Sort by Date descending (newest first)
            history.sort((a, b) -> {
                String dateA = a.get("orderDate") != null ? a.get("orderDate").toString() : "";
                String dateB = b.get("orderDate") != null ? b.get("orderDate").toString() : "";
                return dateB.compareTo(dateA);
            });

            return ResponseEntity.ok(history);
        } catch (Exception ex) {
            ex.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            error.put("cause", ex.getCause() != null ? ex.getCause().toString() : "Unknown");
            
            // To capture the exact line number of the exception:
            if (ex.getStackTrace().length > 0) {
                error.put("trace", ex.getStackTrace()[0].toString());
            }
            return ResponseEntity.status(500).body(error);
        }
    }

    // Update order status for logged in supplier
    @PutMapping("/my-orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestBody java.util.Map<String, String> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName();
        String normalizedEmail = email != null ? email.trim() : "";
        Supplier supplier = supplierRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (supplier == null) return ResponseEntity.status(403).build();

        MedicineOrder order = medicineOrderRepository.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();

        if (!order.getSupplierId().equals(supplier.getId())) {
            return ResponseEntity.status(403).body("You are not authorized to update this order.");
        }

        String newStatus = body.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body("Status is required");
        }
        
        String upperStatus = newStatus.toUpperCase();

        // If order is marked as DELIVERED, update the inventory
        if ("DELIVERED".equals(upperStatus) && !order.getStatus().equals("DELIVERED")) {
            List<org.pgno20.medimart.model.Medicine> meds = 
                medicineRepository.findByNameIgnoreCase(order.getMedicineName());
            if (meds != null && !meds.isEmpty()) {
                org.pgno20.medimart.model.Medicine med = meds.get(0);
                int currentStock = med.getStockQty() != null ? med.getStockQty() : 0;
                med.setStockQty(currentStock + order.getQuantity());
                med.normalizeStatusFromStock();
                medicineRepository.save(med);
                
                // Optional: We can also add a StockBatch for expiry tracking, but for simplicity we just update the total stock here.
            }
        }

        order.setStatus(upperStatus);
        medicineOrderRepository.save(order);
        return ResponseEntity.ok().build();
    }
}