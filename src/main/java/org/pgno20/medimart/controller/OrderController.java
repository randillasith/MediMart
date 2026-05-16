package org.pgno20.medimart.controller;

import jakarta.validation.Valid;
import org.pgno20.medimart.model.Order;
import org.pgno20.medimart.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the Order resource.
 * POST   /api/orders        → place a new order (public, any customer)
 * GET    /api/orders        → list all orders   (admin only — secured in SecurityConfig)
 * PUT    /api/orders/{id}   → update status      (admin only)
 * DELETE /api/orders/{id}   → cancel order       (admin only)
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /** Place a new order. Validates the request body before saving. */
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody Order order) {
        try {
            Order saved = orderService.placeOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to place order: " + e.getMessage()));
        }
    }

    /** List all orders — admin use only (enforced by SecurityConfig). */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /** Update order status. Returns 404 if the order does not exist. */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatus(
            @PathVariable String id,
            @RequestParam String status) {
        try {
            Order updated = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** Cancel and delete an order. Returns 404 if the order does not exist. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable String id) {
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok(Map.of("message", "Order " + id + " cancelled successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
