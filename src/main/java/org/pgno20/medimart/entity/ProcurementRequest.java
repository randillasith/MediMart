package org.pgno20.medimart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "procurement_requests")
public class ProcurementRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long medicineId;

    @Column(nullable = false, length = 120)
    private String medicineName;

    @Column(nullable = false)
    private Integer quantityRequired;

    @Column(nullable = false, length = 20)
    private String status; // OPEN, CLOSED, CANCELLED

    @Column(nullable = true)
    private Double targetPrice;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ProcurementRequest() {
        this.status = "OPEN";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Integer getQuantityRequired() { return quantityRequired; }
    public void setQuantityRequired(Integer quantityRequired) { this.quantityRequired = quantityRequired; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Double targetPrice) { this.targetPrice = targetPrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
