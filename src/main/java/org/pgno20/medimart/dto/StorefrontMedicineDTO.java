package org.pgno20.medimart.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StorefrontMedicineDTO {
    private Long id;
    private String name;
    private String brand;
    private String dosage;
    private BigDecimal minPrice;
    private Long totalStock;
    private String categoryName;
    private LocalDate earliestExpiry;
    private Boolean prescriptionRequired;
    private String imageUrl;
    private String formType;
    private BigDecimal finalPrice;  // Price after tax/dispensing fee
    private String typeLabel;       // "OTC" or "Prescription Required"

    public StorefrontMedicineDTO() {}

    // Constructor used by the GROUP BY storefront query (8 args)
    public StorefrontMedicineDTO(Long id, String name, String brand, String dosage, BigDecimal minPrice, Long totalStock, String categoryName, LocalDate earliestExpiry) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.dosage = dosage;
        this.minPrice = minPrice;
        this.totalStock = totalStock;
        this.categoryName = categoryName;
        this.earliestExpiry = earliestExpiry;
    }

    // Constructor including prescriptionRequired, imageUrl, and formType
    public StorefrontMedicineDTO(Long id, String name, String brand, String dosage, BigDecimal minPrice, Long totalStock, String categoryName, LocalDate earliestExpiry, Boolean prescriptionRequired, String imageUrl, String formType) {
        this(id, name, brand, dosage, minPrice, totalStock, categoryName, earliestExpiry);
        this.prescriptionRequired = prescriptionRequired;
        this.imageUrl = imageUrl;
        this.formType = formType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public Long getTotalStock() { return totalStock; }
    public void setTotalStock(Long totalStock) { this.totalStock = totalStock; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public LocalDate getEarliestExpiry() { return earliestExpiry; }
    public void setEarliestExpiry(LocalDate earliestExpiry) { this.earliestExpiry = earliestExpiry; }

    public Boolean getPrescriptionRequired() { return prescriptionRequired; }
    public void setPrescriptionRequired(Boolean prescriptionRequired) { this.prescriptionRequired = prescriptionRequired; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFormType() { return formType; }
    public void setFormType(String formType) { this.formType = formType; }

    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }

    public String getTypeLabel() { return typeLabel; }
    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }
}
