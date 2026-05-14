package org.pgno20.medimart.entity;

// ==========================================
// OOP CONCEPT 2: POLYMORPHISM via ENUM Strategy
// ==========================================
// SupplierType enum එකේදී polymorphism implement කරනවා
// getLeadDays(), getMarkupRate() - type අනුව වෙනස් behaviour

public enum SupplierType {

    LOCAL("LOCAL") {
        @Override
        public int getLeadDays() { return 2; }

        @Override
        public double getMarkupRate() { return 1.05; } // 5% markup

        @Override
        public String getOrderNotes() {
            return "Local delivery - standard road transport applies";
        }
    },

    IMPORTED("IMPORTED") {
        @Override
        public int getLeadDays() { return 14; }

        @Override
        public double getMarkupRate() { return 1.20; } // 20% import markup

        @Override
        public String getOrderNotes() {
            return "Import clearance required - customs duty applies";
        }
    },

    GOVERNMENT("GOVERNMENT") {
        @Override
        public int getLeadDays() { return 7; }

        @Override
        public double getMarkupRate() { return 1.0; } // no markup

        @Override
        public String getOrderNotes() {
            return "Government tender process - official PO required";
        }
    };

    private final String displayName;

    SupplierType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    // Polymorphic methods - each type overrides
    public abstract int getLeadDays();
    public abstract double getMarkupRate();
    public abstract String getOrderNotes();

    public double calculateOrderTotal(double unitPrice, int qty) {
        return unitPrice * qty * getMarkupRate();
    }
}
