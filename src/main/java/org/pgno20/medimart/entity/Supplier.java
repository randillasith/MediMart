package org.pgno20.medimart.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Arrays;
import java.util.List;

// Supplier extends AbstractSupplier - Abstraction concept use කරනවා
@Entity
@Table(name = "suppliers")
public class Supplier extends org.pgno20.medimart.entity.AbstractSupplier {

    @Id
    @Column(length = 10)
    private String id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank(message = "Type is required")
    @Column(nullable = false, length = 20)
    private String type; // LOCAL / IMPORTED / GOVERNMENT

    @Column(length = 30)
    private String contact;

    @Email(message = "Invalid email format")
    @Column(length = 120)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length=1000)
    private String medicinesSupplied; // "Panadol,Amoxicillin,Metformin"

    public Supplier() {}

    public Supplier(String id, String name, String type, String contact,
                    String email, String address, String medicinesSupplied) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.contact = contact;
        this.email = email;
        this.address = address;
        this.medicinesSupplied = medicinesSupplied;
    }

    @Override
    public String getSupplierCategory() {
        SupplierType supplierType = getSupplierTypeEnum();
        return supplierType.getDisplayName() + " (Lead: " + supplierType.getLeadDays() + " days)";
    }

    @Override
    public String displaySupplyList() {
        // POLYMORPHISM - type අනුව output වෙනස් වෙනවා
        SupplierType supplierType = getSupplierTypeEnum();
        return supplierType.getDisplayName() + " Supply List: " + medicinesSupplied;
    }

    @Override
    protected String buildOrderSummary(String medicineName, int quantity) {
        SupplierType supplierType = getSupplierTypeEnum();
        return String.format("Order placed with %s for %d units of %s. %s",
                name, quantity, medicineName, supplierType.getOrderNotes());
    }

    // ==========================================
    // Helper - string type to enum convert
    // ==========================================
    public SupplierType getSupplierTypeEnum() {
        try {
            return SupplierType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return SupplierType.WHOLESALER;
        }
    }

    // Medicine list parse කිරීම - admin UI එකට
    public List<String> getMedicineList() {
        if (medicinesSupplied == null || medicinesSupplied.isBlank()) return List.of();
        return Arrays.asList(medicinesSupplied.split(","));
    }

    public boolean suppliesMedicine(String medicineName) {
        return getMedicineList().stream()
                .anyMatch(m -> m.trim().equalsIgnoreCase(medicineName.trim()));
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMedicinesSupplied() { return medicinesSupplied; }
    public void setMedicinesSupplied(String medicinesSupplied) { this.medicinesSupplied = medicinesSupplied; }
}