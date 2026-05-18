package org.pgno20.medimart.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@DiscriminatorValue("OTC")
public class OTCMedicine extends Medicine {

    public OTCMedicine() {
        setPrescriptionRequired(false);
    }

    @Override
    public String getTypeLabel() {
        return "OTC";
    }

    @Override
    public BigDecimal getFinalPrice() {
        if (getPrice() == null) return BigDecimal.ZERO;
        // OTC medicines get a 10% tax added
        return getPrice().multiply(new BigDecimal("1.10")).setScale(2, RoundingMode.HALF_UP);
    }
}