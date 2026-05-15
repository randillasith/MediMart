package org.pgno20.medimart.dto;

public class SupplierResponse {

    private String id;
    private String name;
    private String type;
    private String contact;
    private String email;
    private String address;
    private String medicinesSupplied;
    private String supplierCategory;

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

    public String getSupplierCategory() { return supplierCategory; }
    public void setSupplierCategory(String supplierCategory) { this.supplierCategory = supplierCategory; }
}
