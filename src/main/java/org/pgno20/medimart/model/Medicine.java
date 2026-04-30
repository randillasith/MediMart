package org.pgno20.medimart.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "medicines")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public abstract class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=40)
    private String sku; // MED001

    @Column(nullable=false, length=150)
    private String name;

    @Column(length=120)
    private String brand;

    @Column(length=50)
    private String dosage; // e.g. 500mg

    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal price;

    @Column(nullable=false)
    private Integer stockQty;

    private LocalDate expiryDate;

    @Column(nullable=false)
    private Boolean prescriptionRequired = false;

    @Column(nullable=false, length=30)
    private String status = "AVAILABLE"; // AVAILABLE / OUT_OF_STOCK / DISCONTINUED

    @Column(length=255)
    private String imageUrl;

    @ManyToOne(optional=false)
    @JoinColumn(name = "category_id")
    private Category category;

    protected Medicine() {}

    // Polymorphism example: subclasses can change label and calculate price differently
    public abstract String getTypeLabel();
    public abstract BigDecimal getFinalPrice();

    // Common helpers
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public void normalizeStatusFromStock() {
        if (stockQty != null && stockQty <= 0) status = "OUT_OF_STOCK";
        else if (!"DISCONTINUED".equals(status)) status = "AVAILABLE";
    }

    // getters/setters
    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getDosage() { return dosage; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQty() { return stockQty; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public Boolean getPrescriptionRequired() { return prescriptionRequired; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public Category getCategory() { return category; }

    public void setId(Long id) { this.id = id; }
    public void setSku(String sku) { this.sku = sku; }
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setPrescriptionRequired(Boolean prescriptionRequired) { this.prescriptionRequired = prescriptionRequired; }
    public void setStatus(String status) { this.status = status; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(Category category) { this.category = category; }
}