package org.pgno20.medimart.service;

import org.pgno20.medimart.model.Order;
import org.pgno20.medimart.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // Create Order
    public Order placeOrder(Order order) {
        return orderRepository.save(order);
    }

    // Read All Orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Update Order Status
    public Order updateOrderStatus(String id, String newStatus) {
        Optional<Order> existingOrder = orderRepository.findById(id);
        if (existingOrder.isPresent()) {
            Order order = existingOrder.get();
            order.setStatus(newStatus);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found!");
    }

    // Delete / Cancel Order
    public void cancelOrder(String id) {
        orderRepository.deleteById(id);
    }
}