package org.pgno20.medimart.service;

import org.pgno20.medimart.model.SystemSettings;
import org.pgno20.medimart.repository.SystemSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemSettingsService {

    private final SystemSettingsRepository repo;

    public SystemSettingsService(SystemSettingsRepository repo) {
        this.repo = repo;
    }

    /**
     * Returns the singleton settings row, creating it with defaults if it doesn't exist yet.
     */
    public SystemSettings getSettings() {
        return repo.findById(1L).orElseGet(() -> repo.save(new SystemSettings()));
    }

    /**
     * Saves updated settings. Always uses id=1 to enforce the singleton pattern.
     */
    @Transactional
    public SystemSettings saveSettings(SystemSettings updated, String modifiedBy) {
        SystemSettings current = getSettings();
        current.setOtcTaxRate(updated.getOtcTaxRate());
        current.setPrescriptionDispensingFee(updated.getPrescriptionDispensingFee());
        current.setStandardDeliveryFee(updated.getStandardDeliveryFee());
        current.setLowStockThreshold(updated.getLowStockThreshold());
        current.setMaintenanceMode(updated.isMaintenanceMode());
        current.setAutoBackupEnabled(updated.isAutoBackupEnabled());
        current.setLastModifiedBy(modifiedBy);
        return repo.save(current);
    }
}
