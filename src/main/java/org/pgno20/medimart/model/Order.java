package org.pgno20.medimart.model;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Order {

    @Id
    private String orderId;
    private String customerName;
    private String medicineDetails;
    private int quantity;
    private double totalPrice;
    private String status;

    public Order() {}

    public Order(String orderId, String customerName, String medicineDetails, int quantity, double totalPrice, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.medicineDetails = medicineDetails;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    // Encapsulation: Controlled access
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getMedicineDetails() { return medicineDetails; }
    public void setMedicineDetails(String medicineDetails) { this.medicineDetails = medicineDetails; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Polymorphism: Abstract method for different workflows
    public abstract double calculateDiscount();
}



