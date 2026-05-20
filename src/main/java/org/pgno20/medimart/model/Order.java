package org.pgno20.medimart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a customer order placed through the MediMart storefront.
 * Inheritance and DiscriminatorColumn removed — no subclasses exist yet.
 * Uses BigDecimal for monetary precision instead of double.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(length = 40)
    private String orderId;

    @NotBlank(message = "Customer name is required")
    @Column(nullable = false, length = 200)
    private String customerName;

    /** Email of the placing user — used to link orders back to the user profile. */
    @Column(length = 200)
    private String customerEmail;

    /**
     * Free-text summary of ordered medicines, e.g. "Paracetamol 500mg (x2), Amoxicillin (x1)".
     * Future improvement: replace with a proper OrderItem join table.
     */
    @Column(columnDefinition = "TEXT")
    private String medicineDetails;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private int quantity;

    @DecimalMin(value = "0.0", message = "Total price cannot be negative")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal deliveryFee = new BigDecimal("300.00");

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    /** Shipping address captured from the checkout form — persisted for dispatch. */
    @Column(length = 500)
    private String shippingAddress;

    /** Whether a prescription was submitted with this order. */
    @Column(nullable = false)
    private boolean prescriptionSubmitted = false;

    /** Tracks if the order contains any prescription-required medicines. */
    @Column(name = "has_prescription_items", nullable = false)
    private boolean hasPrescriptionItems = false;

    /** The specific prescription ID associated with this order (e.g. RX001) */
    @Column(name = "prescription_id", length = 20)
    private String prescriptionId;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<OrderItem> items = new java.util.ArrayList<>();

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "PENDING";
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Order() {}

    public Order(String orderId, String customerName, String medicineDetails,
                 int quantity, BigDecimal totalPrice, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.medicineDetails = medicineDetails;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Polymorphism: subclasses can override to apply category-specific discounts
    public double calculateDiscount() {
        return 0.0;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getMedicineDetails() { return medicineDetails; }
    public void setMedicineDetails(String medicineDetails) { this.medicineDetails = medicineDetails; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BigDecimal getDeliveryFee() {
        return deliveryFee != null ? deliveryFee : new BigDecimal("300.00");
    }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public BigDecimal getSubtotal() {
        if (totalPrice == null) return BigDecimal.ZERO;
        return totalPrice.subtract(getDeliveryFee()).max(BigDecimal.ZERO);
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(this::addItem);
        }
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public boolean isPrescriptionSubmitted() { return prescriptionSubmitted; }
    public void setPrescriptionSubmitted(boolean prescriptionSubmitted) { this.prescriptionSubmitted = prescriptionSubmitted; }

    public boolean isHasPrescriptionItems() { return hasPrescriptionItems; }
    public void setHasPrescriptionItems(boolean hasPrescriptionItems) { this.hasPrescriptionItems = hasPrescriptionItems; }

    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }


    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
