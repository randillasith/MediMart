package org.pgno20.medimart.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

// ==========================================
// OOP CONCEPT 3: ENCAPSULATION (full encapsulation + business logic inside entity)
// ==========================================

@Entity
@Table(name = "medicine_orders")
public class MedicineOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false, length = 10)
    private String supplierId; // FK as string (supplier id)

    @Column(nullable = false, length = 120)
    private String supplierName;

    @Column(nullable = false, length = 120)
    private String medicineName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private Double totalPrice;

    @Column(nullable = false, length = 20)
    private String supplierType; // LOCAL / IMPORTED / GOVERNMENT

    @Column(nullable = false, length = 20)
    private String status; // PENDING / APPROVED / REJECTED / DELIVERED

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column
    private LocalDate expectedDelivery;

    @Column(length = 500)
    private String adminNotes;

    public MedicineOrder() {
        this.status = "PENDING";
        this.orderDate = LocalDate.now();
    }

    // ==========================================
    // Business logic encapsulated inside entity
    // ==========================================

    public void calculateAndSetTotals(SupplierType type) {
        double markup = type.getMarkupRate();
        this.totalPrice = this.unitPrice * this.quantity * markup;
        this.expectedDelivery = this.orderDate.plusDays(type.getLeadDays());
    }

    public boolean isPending() { return "PENDING".equals(this.status); }
    public boolean isApproved() { return "APPROVED".equals(this.status); }

    public void approve(String notes) {
        if (!isPending()) throw new IllegalStateException("Only PENDING orders can be approved");
        this.status = "APPROVED";
        this.adminNotes = notes;
    }

    public void reject(String reason) {
        if (!isPending()) throw new IllegalStateException("Only PENDING orders can be rejected");
        this.status = "REJECTED";
        this.adminNotes = reason;
    }

    public void markDelivered() {
        if (!isApproved()) throw new IllegalStateException("Only APPROVED orders can be delivered");
        this.status = "DELIVERED";
    }

    // Getters & Setters (full encapsulation)
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }

    public String getSupplierType() { return supplierType; }
    public void setSupplierType(String supplierType) { this.supplierType = supplierType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public LocalDate getExpectedDelivery() { return expectedDelivery; }
    public void setExpectedDelivery(LocalDate expectedDelivery) { this.expectedDelivery = expectedDelivery; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
}
