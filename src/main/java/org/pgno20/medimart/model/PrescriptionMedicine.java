package org.pgno20.medimart.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("PRESCRIPTION")
public class PrescriptionMedicine extends Medicine {

    public PrescriptionMedicine() {
        setPrescriptionRequired(true);
    }

    @Override
    public String getTypeLabel() {
        return "Prescription Required";
    }

    @Override
    public BigDecimal getFinalPrice() {
        if (getPrice() == null) return BigDecimal.ZERO;
        // Prescription medicines have a flat $5.00 dispensing fee, no tax
        return getPrice().add(new BigDecimal("5.00"));
    }
}