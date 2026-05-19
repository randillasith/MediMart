package org.pgno20.medimart.controller;

import org.pgno20.medimart.entity.MedicineOrder;
import org.pgno20.medimart.service.MedicineOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class MedicineOrderController {

    private final MedicineOrderService orderService;

    public MedicineOrderController(MedicineOrderService orderService) {
        this.orderService = orderService;
    }

    // POST /api/orders/place
    @PostMapping("/place")
    public ResponseEntity<MedicineOrder> placeOrder(@RequestBody Map<String, Object> body) {
        String supplierId = (String) body.get("supplierId");
        String medicineName = (String) body.get("medicineName");
        int quantity = (Integer) body.get("quantity");
        double unitPrice = Double.parseDouble(body.get("unitPrice").toString());

        LocalDate expectedDelivery = null;
        if (body.containsKey("expectedDelivery")) {
            Object val = body.get("expectedDelivery");
            if (val != null && !val.toString().isBlank()) {
                expectedDelivery = LocalDate.parse(val.toString());
            }
        }

        String deliveryMode = null;
        if (body.containsKey("deliveryMode")) {
            Object val = body.get("deliveryMode");
            if (val != null) {
                deliveryMode = val.toString();
            }
        }

        MedicineOrder order = orderService.placeOrder(supplierId, medicineName, quantity, unitPrice, expectedDelivery, deliveryMode);
        
        return ResponseEntity.ok(order);
    }

    // GET /api/orders
    @GetMapping
    public ResponseEntity<List<MedicineOrder>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // GET /api/orders/supplier/{supplierId}
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<MedicineOrder>> getBySupplier(@PathVariable String supplierId) {
        return ResponseEntity.ok(orderService.getOrdersBySupplier(supplierId));
    }

    // GET /api/orders/status/{status}
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MedicineOrder>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    // PUT /api/orders/{id}/approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<MedicineOrder> approve(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.approveOrder(id, body.getOrDefault("notes", "")));
    }

    // PUT /api/orders/{id}/reject
    @PutMapping("/{id}/reject")
    public ResponseEntity<MedicineOrder> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.rejectOrder(id, body.getOrDefault("reason", "")));
    }

    // PUT /api/orders/{id}/delivered
    @PutMapping("/{id}/delivered")
    public ResponseEntity<MedicineOrder> delivered(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.markDelivered(id));
    }

    // GET /api/orders/supplier/{supplierId}/medicines
    @GetMapping("/supplier/{supplierId}/medicines")
    public ResponseEntity<List<String>> getSupplierMedicines(@PathVariable String supplierId) {
        return ResponseEntity.ok(orderService.getSupplierMedicines(supplierId));
    }

    // ==========================================
    // PUT /api/orders/{id}
    // Edit order — only PENDING orders can be edited
    // ==========================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        MedicineOrder order = orderService.getOrderById(id);

        if (!order.getStatus().equals("PENDING")) {
            return ResponseEntity.badRequest()
                    .body("Only PENDING orders can be edited.");
        }

        if (body.containsKey("medicineName"))
            order.setMedicineName((String) body.get("medicineName"));

        if (body.containsKey("quantity"))
            order.setQuantity((Integer) body.get("quantity"));

        if (body.containsKey("unitPrice"))
            order.setUnitPrice(Double.parseDouble(body.get("unitPrice").toString()));

        if (body.containsKey("totalPrice"))
            order.setTotalPrice(Double.parseDouble(body.get("totalPrice").toString()));

        if (body.containsKey("expectedDelivery")) {
            Object val = body.get("expectedDelivery");
            order.setExpectedDelivery(val != null ? LocalDate.parse(val.toString()) : null);
        }

        if (body.containsKey("adminNotes"))
            order.setAdminNotes((String) body.get("adminNotes"));
            
        if (body.containsKey("deliveryMode"))
            order.setDeliveryMode((String) body.get("deliveryMode"));

        return ResponseEntity.ok(orderService.saveOrder(order));
    }

    // ==========================================
    // DELETE /api/orders/{id}
    // Delete order — DELIVERED orders cannot be deleted
    // ==========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {

        MedicineOrder order = orderService.getOrderById(id);

        if (order.getStatus().equals("DELIVERED")) {
            return ResponseEntity.badRequest()
                    .body("Completed orders cannot be deleted.");
        }

        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // PUT /api/orders/{id}/confirm
    // Supplier confirms the order with price and delivery date
    // ==========================================
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        
        MedicineOrder order = orderService.getOrderById(id);
        
        if (!order.isPending()) {
            return ResponseEntity.badRequest().body("Only PENDING orders can be confirmed.");
        }
        
        if (body.containsKey("unitPrice")) {
            order.setUnitPrice(Double.parseDouble(body.get("unitPrice").toString()));
            if (order.getQuantity() != null) {
                order.setTotalPrice(order.getUnitPrice() * order.getQuantity());
            }
        }
        
        if (body.containsKey("expectedDelivery")) {
            Object val = body.get("expectedDelivery");
            order.setExpectedDelivery(val != null ? LocalDate.parse(val.toString()) : null);
        }
        
        String notes = body.containsKey("notes") ? (String) body.get("notes") : "Supplier confirmed order";
        order.approve(notes);
        
        return ResponseEntity.ok(orderService.saveOrder(order));
    }
}