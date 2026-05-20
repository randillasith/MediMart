package org.pgno20.medimart.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_bids")
public class SupplierBid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", nullable = false)
    private ProcurementRequest procurementRequest;

    @Column(nullable = false, length = 10)
    private String supplierId;

    @Column(nullable = false, length = 120)
    private String supplierName;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, ACCEPTED, REJECTED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public SupplierBid() {
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProcurementRequest getProcurementRequest() { return procurementRequest; }
    public void setProcurementRequest(ProcurementRequest procurementRequest) { this.procurementRequest = procurementRequest; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
