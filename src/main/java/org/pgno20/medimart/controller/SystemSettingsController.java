package org.pgno20.medimart.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.pgno20.medimart.model.SystemSettings;
import org.pgno20.medimart.service.SystemSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for System Settings.
 * GET  /api/settings  — load current configuration (admin only)
 * PUT  /api/settings  — save updated configuration (admin only)
 */
@RestController
@RequestMapping("/api/settings")
public class SystemSettingsController {

    private final SystemSettingsService settingsService;

    public SystemSettingsController(SystemSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<SystemSettings> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    /**
     * Public endpoint — returns only customer-facing settings (tax rate, delivery fee).
     * No authentication required so checkout.html and catalog can read them.
     */
    @GetMapping("/public")
    public ResponseEntity<java.util.Map<String, Object>> getPublicSettings() {
        var s = settingsService.getSettings();
        return ResponseEntity.ok(java.util.Map.of(
            "otcTaxRate",             s.getOtcTaxRate(),
            "prescriptionDispensingFee", s.getPrescriptionDispensingFee(),
            "standardDeliveryFee",    s.getStandardDeliveryFee()
        ));
    }

    @PutMapping
    public ResponseEntity<?> saveSettings(@RequestBody SystemSettings settings,
                                           HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String modifiedBy = "Admin";
        if (session != null && session.getAttribute("userFullName") != null) {
            modifiedBy = (String) session.getAttribute("userFullName");
        }
        try {
            SystemSettings saved = settingsService.saveSettings(settings, modifiedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Settings saved successfully",
                "settings", saved
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Failed to save settings: " + e.getMessage()));
        }
    }
}
