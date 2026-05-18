package org.pgno20.medimart.controller;

import org.pgno20.medimart.dto.DashboardMetricsDTO;
import org.pgno20.medimart.model.Medicine;
import org.pgno20.medimart.model.Order;
import org.pgno20.medimart.repository.MedicineRepository;
import org.pgno20.medimart.repository.OrderRepository;
import org.pgno20.medimart.repository.UserRepository;
import org.pgno20.medimart.service.SystemSettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final SystemSettingsService settingsService;

    public DashboardController(OrderRepository orderRepository, UserRepository userRepository,
                               MedicineRepository medicineRepository, SystemSettingsService settingsService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.medicineRepository = medicineRepository;
        this.settingsService = settingsService;
    }

    @GetMapping("/metrics")
    public DashboardMetricsDTO getMetrics() {
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        long totalOrders = orderRepository.countByStatusNot("CANCELLED");
        long pendingOrders = orderRepository.countByStatus("PENDING");
        long processingOrders = orderRepository.countByStatus("PROCESSING");
        long totalUsers = userRepository.count();
        int threshold = settingsService.getSettings().getLowStockThreshold();
        List<Medicine> lowStockItems = medicineRepository.findLowStockItems(threshold);
        long lowStockCount = lowStockItems.size();

        return new DashboardMetricsDTO(totalRevenue, totalOrders, pendingOrders, processingOrders, totalUsers, lowStockCount);
    }

    @GetMapping("/recent-orders")
    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByCreatedAtDesc();
    }

    @GetMapping("/low-stock")
    public List<Map<String, Object>> getLowStockItems() {
        int threshold = settingsService.getSettings().getLowStockThreshold();
        List<Medicine> items = medicineRepository.findLowStockItems(threshold);
        return items.stream().map(medicine -> Map.<String, Object>of(
                "id", medicine.getId(),
                "name", medicine.getName(),
                "totalQuantity", medicine.getStockQty(),
                "categoryId", medicine.getCategory().getName()
        )).collect(Collectors.toList());
    }

    @GetMapping("/analytics")
    public Map<String, Object> getAnalytics(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "7") int days) {
        BigDecimal rxRevenue = orderRepository.calculateTotalRevenueByPrescription(true);
        BigDecimal otcRevenue = orderRepository.calculateTotalRevenueByPrescription(false);
        if (rxRevenue == null) rxRevenue = BigDecimal.ZERO;
        if (otcRevenue == null) otcRevenue = BigDecimal.ZERO;

        java.time.LocalDateTime startDate = java.time.LocalDateTime.now().minusDays(days - 1).withHour(0).withMinute(0).withSecond(0);
        List<Order> recentOrders = orderRepository.findDeliveredOrdersSince(startDate);

        Map<java.time.LocalDate, BigDecimal> dailyRevenueMap = recentOrders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalPrice, BigDecimal::add)
                ));

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd");

        List<Map<String, Object>> dailyRevenue = java.util.stream.IntStream.range(0, days).mapToObj(i -> {
            java.time.LocalDate date = java.time.LocalDate.now().minusDays((days - 1) - i);
            BigDecimal rev = dailyRevenueMap.getOrDefault(date, BigDecimal.ZERO);
            return Map.<String, Object>of(
                    "date", date.toString(),
                    "shortDate", date.format(formatter), 
                    "revenue", rev
            );
        }).collect(Collectors.toList());

        return Map.of(
                "dailyRevenue", dailyRevenue,
                "prescriptionRevenue", rxRevenue,
                "otcRevenue", otcRevenue
        );
    }
}
