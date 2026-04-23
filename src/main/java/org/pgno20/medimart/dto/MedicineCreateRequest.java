package org.pgno20.medimart.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicineCreateRequest {

    @NotBlank
    private String type; // OTC or PRESCRIPTION

    @NotBlank
    private String sku;

    @NotBlank
    private String name;

    private String brand;
    private String dosage;

    @NotNull @DecimalMin("0.01")
    private BigDecimal price;

    @NotNull @Min(0)
    private Integer stockQty;

    private LocalDate expiryDate;

    @NotNull
    private Long categoryId;

    // getters/setters
    public String getType() { return type; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getDosage() { return dosage; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQty() { return stockQty; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public Long getCategoryId() { return categoryId; }

    public void setType(String type) { this.type = type; }
    public void setSku(String sku) { this.sku = sku; }
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}