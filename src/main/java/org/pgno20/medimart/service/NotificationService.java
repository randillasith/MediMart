package org.pgno20.medimart.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    public void triggerLowStockAlert(String medicineName, Long medicineId) {
        System.out.println("Low Stock Alert: " + medicineName + " (ID: " + medicineId + ")");
    }

    public void clearLowStockAlert(Long medicineId) {
        System.out.println("Cleared Low Stock Alert for ID: " + medicineId);
    }
}
