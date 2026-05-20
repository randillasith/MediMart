package org.pgno20.medimart.dto;

import java.math.BigDecimal;

public class DashboardMetricsDTO {
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long pendingOrders;
    private long processingOrders;
    private long totalUsers;
    private long lowStockItems;

    public DashboardMetricsDTO() {
    }

    public DashboardMetricsDTO(BigDecimal totalRevenue, long totalOrders, long pendingOrders, long processingOrders, long totalUsers, long lowStockItems) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.processingOrders = processingOrders;
        this.totalUsers = totalUsers;
        this.lowStockItems = lowStockItems;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getProcessingOrders() {
        return processingOrders;
    }

    public void setProcessingOrders(long processingOrders) {
        this.processingOrders = processingOrders;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(long lowStockItems) {
        this.lowStockItems = lowStockItems;
    }
}
