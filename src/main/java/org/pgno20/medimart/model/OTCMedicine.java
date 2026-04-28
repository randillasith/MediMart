package org.pgno20.medimart.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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
}