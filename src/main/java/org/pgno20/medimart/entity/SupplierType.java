package org.pgno20.medimart.entity;

// ==========================================
// OOP CONCEPT 2: POLYMORPHISM via ENUM Strategy
// ==========================================
// SupplierType enum එකේදී polymorphism implement කරනවා
// getLeadDays(), getMarkupRate() - type අනුව වෙනස් behaviour

public enum SupplierType {

    WHOLESALER("Wholesale Distributor") {
        @Override
        public int getLeadDays() { return 3; }

        @Override
        public double getMarkupRate() { return 1.08; } // 8% markup

        @Override
        public String getOrderNotes() {
            return "Wholesale bulk order - standard distribution channel";
        }
    },

    MANUFACTURER("Direct Manufacturer") {
        @Override
        public int getLeadDays() { return 7; }

        @Override
        public double getMarkupRate() { return 1.05; } // 5% markup

        @Override
        public String getOrderNotes() {
            return "Direct from manufacturer - quality certification required";
        }
    },

    SPECIALIST("Specialist Pharmacy") {
        @Override
        public int getLeadDays() { return 5; }

        @Override
        public double getMarkupRate() { return 1.15; } // 15% markup

        @Override
        public String getOrderNotes() {
            return "Specialist supply - controlled substance protocols may apply / Specialized transport required - temperature controlled";
        }
    },

    LOGISTICS("Logistics Partner") {
        @Override
        public int getLeadDays() { return 2; }

        @Override
        public double getMarkupRate() { return 1.10; } // 10% markup

        @Override
        public String getOrderNotes() {
            return "Logistics partner - cold chain transport available";
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