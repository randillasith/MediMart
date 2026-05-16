package org.pgno20.medimart.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.pgno20.medimart.validation.MinimumShelfLife;

public class StockBatchRequest {

    @NotNull @Min(1)
    private Integer quantity;

    @MinimumShelfLife(months = 3)
    private LocalDate expiryDate;

    @DecimalMin("0.01")
    private BigDecimal purchasePrice;

    private String supplierId;

    // Getters & Setters
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
}
