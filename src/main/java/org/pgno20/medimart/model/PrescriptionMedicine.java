package org.pgno20.medimart.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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
}