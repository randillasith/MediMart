package org.pgno20.medimart.controller;

import org.pgno20.medimart.entity.MedicineOrder;
import org.pgno20.medimart.service.MedicineOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class MedicineOrderController {

    private final MedicineOrderService orderService;

    public MedicineOrderController(MedicineOrderService orderService) {
        this.orderService = orderService;
    }

    // ==========================================
    // Admin order place කිරීම
    // POST /api/orders/place
    // Body: { supplierId, medicineName, quantity, unitPrice }
    // ==========================================
    @PostMapping("/place")
    public ResponseEntity<MedicineOrder> placeOrder(@RequestBody Map<String, Object> body) {
        String supplierId = (String) body.get("supplierId");
        String medicineName = (String) body.get("medicineName");

        if (supplierId == null || medicineName == null || body.get("quantity") == null || body.get("unitPrice") == null) {
            throw new IllegalArgumentException("Missing required fields: supplierId, medicineName, quantity, unitPrice");
        }

        int quantity = Integer.parseInt(body.get("quantity").toString());
        double unitPrice = Double.parseDouble(body.get("unitPrice").toString());

        MedicineOrder order = orderService.placeOrder(supplierId, medicineName, quantity, unitPrice);
        return ResponseEntity.ok(order);
    }

    // All orders list
    @GetMapping
    public ResponseEntity<List<MedicineOrder>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // Supplier orders
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<MedicineOrder>> getBySupplier(@PathVariable String supplierId) {
        return ResponseEntity.ok(orderService.getOrdersBySupplier(supplierId));
    }

    // Status filter - PENDING / APPROVED / REJECTED / DELIVERED
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MedicineOrder>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    // Approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<MedicineOrder> approve(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.approveOrder(id, body.getOrDefault("notes", "")));
    }

    // Reject
    @PutMapping("/{id}/reject")
    public ResponseEntity<MedicineOrder> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.rejectOrder(id, body.getOrDefault("reason", "")));
    }

    // Mark delivered
    @PutMapping("/{id}/delivered")
    public ResponseEntity<MedicineOrder> delivered(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markDelivered(id));
    }

    // Supplier medicines list - order form fill කරන්න
    @GetMapping("/supplier/{supplierId}/medicines")
    public ResponseEntity<List<String>> getSupplierMedicines(@PathVariable String supplierId) {
        return ResponseEntity.ok(orderService.getSupplierMedicines(supplierId));
    }
}
