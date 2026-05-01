package org.pgno20.medimart.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicineResponse {
    private Long id;
    private String sku;
    private String name;
    private String brand;
    private String dosage;
    private String formType;
    private BigDecimal price;
    private BigDecimal finalPrice;
    private Integer stockQty;
    private LocalDate expiryDate;
    private boolean prescriptionRequired;
    private String status;
    private String typeLabel;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
    private long batchCount;

    // getters/setters
    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getDosage() { return dosage; }
    public String getFormType() { return formType; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public Integer getStockQty() { return stockQty; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public boolean isPrescriptionRequired() { return prescriptionRequired; }
    public String getStatus() { return status; }
    public String getTypeLabel() { return typeLabel; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getImageUrl() { return imageUrl; }
    public long getBatchCount() { return batchCount; }

    public void setId(Long id) { this.id = id; }
    public void setSku(String sku) { this.sku = sku; }
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setFormType(String formType) { this.formType = formType; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setPrescriptionRequired(boolean prescriptionRequired) { this.prescriptionRequired = prescriptionRequired; }
    public void setStatus(String status) { this.status = status; }
    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setBatchCount(long batchCount) { this.batchCount = batchCount; }
}