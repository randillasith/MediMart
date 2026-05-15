package org.pgno20.medimart.service;

import org.pgno20.medimart.entity.MedicineOrder;
import org.pgno20.medimart.entity.Supplier;
import org.pgno20.medimart.entity.SupplierType;
import org.pgno20.medimart.repository.MedicineOrderRepository;
import org.pgno20.medimart.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicineOrderService {

    private final MedicineOrderRepository orderRepository;
    private final SupplierRepository supplierRepository;

    public MedicineOrderService(MedicineOrderRepository orderRepository,
                                SupplierRepository supplierRepository) {
        this.orderRepository = orderRepository;
        this.supplierRepository = supplierRepository;
    }

    // ==========================================
    // Admin order create කිරීම
    // Supplier validate කරලා, medicine check කරලා order හදනවා
    // ==========================================
    public MedicineOrder placeOrder(String supplierId, String medicineName,
                                    int quantity, double unitPrice) {

        // Supplier exist කරනවාද check
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));

        // Supplier ඒ medicine supply කරනවාද check (Abstraction method use)
        if (!supplier.suppliesMedicine(medicineName)) {
            throw new RuntimeException(
                    supplier.getName() + " does not supply: " + medicineName +
                            ". Available: " + supplier.getMedicinesSupplied()
            );
        }

        // MedicineOrder object create
        MedicineOrder order = new MedicineOrder();
        order.setSupplierId(supplierId);
        order.setSupplierName(supplier.getName());
        order.setMedicineName(medicineName);
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setSupplierType(supplier.getType());

        // POLYMORPHISM: SupplierType enum use කරලා total + delivery date calculate
        SupplierType type = supplier.getSupplierTypeEnum();
        order.calculateAndSetTotals(type);

        return orderRepository.save(order);
    }

    // All orders
    public List<MedicineOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    // Supplier orders
    public List<MedicineOrder> getOrdersBySupplier(String supplierId) {
        return orderRepository.findBySupplierId(supplierId);
    }

    // Status filter
    public List<MedicineOrder> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status.toUpperCase());
    }

    // Approve order (Encapsulation - logic inside entity)
    public MedicineOrder approveOrder(Long orderId, String notes) {
        MedicineOrder order = getOrderById(orderId);
        order.approve(notes); // Entity method use - encapsulation
        return orderRepository.save(order);
    }

    // Reject order
    public MedicineOrder rejectOrder(Long orderId, String reason) {
        MedicineOrder order = getOrderById(orderId);
        order.reject(reason);
        return orderRepository.save(order);
    }

    // Mark delivered
    public MedicineOrder markDelivered(Long orderId) {
        MedicineOrder order = getOrderById(orderId);
        order.markDelivered();
        return orderRepository.save(order);
    }

    // Get available medicines from a supplier
    public List<String> getSupplierMedicines(String supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));
        return supplier.getMedicineList();
    }

    private MedicineOrder getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }
}
