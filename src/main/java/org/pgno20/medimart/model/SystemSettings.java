package org.pgno20.medimart.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Singleton entity that stores global system configuration settings.
 * There is always exactly one row with id=1.
 */
@Entity
@Table(name = "system_settings")
public class SystemSettings {

    @Id
    private Long id = 1L; // Always 1 — singleton pattern

    /** OTC tax rate as a percentage e.g. 10.0 = 10% */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal otcTaxRate = new BigDecimal("10.00");

    /** Fixed dispensing fee added to every prescription order */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prescriptionDispensingFee = new BigDecimal("5.00");

    /** Flat delivery fee for standard orders */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal standardDeliveryFee = new BigDecimal("12.50");

    /** Units below which an item is flagged as low stock */
    @Column(nullable = false)
    private int lowStockThreshold = 20;

    /** If true, the storefront should be taken offline */
    @Column(nullable = false)
    private boolean maintenanceMode = false;

    /** If true, scheduled automatic DB backups are enabled */
    @Column(nullable = false)
    private boolean autoBackupEnabled = true;

    /** Timestamp of last update (managed manually) */
    @Column
    private java.time.LocalDateTime lastModifiedAt;

    /** Who last changed settings */
    @Column(length = 200)
    private String lastModifiedBy;

    @PrePersist
    @PreUpdate
    void touch() {
        this.lastModifiedAt = java.time.LocalDateTime.now();
    }

    public SystemSettings() {}

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }

    public BigDecimal getOtcTaxRate() { return otcTaxRate; }
    public void setOtcTaxRate(BigDecimal otcTaxRate) { this.otcTaxRate = otcTaxRate; }

    public BigDecimal getPrescriptionDispensingFee() { return prescriptionDispensingFee; }
    public void setPrescriptionDispensingFee(BigDecimal prescriptionDispensingFee) { this.prescriptionDispensingFee = prescriptionDispensingFee; }

    public BigDecimal getStandardDeliveryFee() { return standardDeliveryFee; }
    public void setStandardDeliveryFee(BigDecimal standardDeliveryFee) { this.standardDeliveryFee = standardDeliveryFee; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public boolean isMaintenanceMode() { return maintenanceMode; }
    public void setMaintenanceMode(boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }

    public boolean isAutoBackupEnabled() { return autoBackupEnabled; }
    public void setAutoBackupEnabled(boolean autoBackupEnabled) { this.autoBackupEnabled = autoBackupEnabled; }

    public java.time.LocalDateTime getLastModifiedAt() { return lastModifiedAt; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
}
