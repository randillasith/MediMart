package org.pgno20.medimart.entity;

// ==========================================
// OOP CONCEPT 1: ABSTRACTION (Abstract Class)
// ==========================================
// AbstractSupplier එකේ common behavior define කරනවා
// Supplier entity මේකෙන් extend වෙනවා

public abstract class AbstractSupplier {

    // Abstract method - subclass mandatory implement කරන්න ඕනෙ
    public abstract String getSupplierCategory();

    // Abstract method - supply list display කිරීම
    public abstract String displaySupplyList();

    // Concrete method - common logic (override කරන්න ඕනෙ නෑ)
    public String getFormattedInfo(String id, String name, String type) {
        return String.format("[%s] %s (%s) - Category: %s", id, name, type, getSupplierCategory());
    }

    // Template method pattern - ordering process
    public final String initiateOrder(String medicineName, int quantity) {
        String validation = validateOrder(medicineName, quantity);
        if (validation != null) return "ORDER FAILED: " + validation;
        return buildOrderSummary(medicineName, quantity);
    }

    protected String validateOrder(String medicineName, int quantity) {
        if (medicineName == null || medicineName.isBlank()) return "Medicine name required";
        if (quantity <= 0) return "Quantity must be > 0";
        return null; // valid
    }

    protected abstract String buildOrderSummary(String medicineName, int quantity);
}
