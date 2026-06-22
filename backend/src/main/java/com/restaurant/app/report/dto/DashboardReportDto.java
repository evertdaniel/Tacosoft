package com.restaurant.app.report.dto;

import java.math.BigDecimal;

/**
 * Dashboard report DTO with sales summary, active orders, table occupancy, and low stock alerts.
 * Implements SPEC-REPORT-001.
 */
public class DashboardReportDto {

    private Integer occupiedTables;
    private Integer activeOrders;
    private Integer closedOrdersToday;
    private BigDecimal salesToday;
    private Integer totalTables;
    private Integer lowStockProducts;

    public DashboardReportDto() {}

    public Integer getOccupiedTables() {
        return occupiedTables;
    }

    public void setOccupiedTables(Integer occupiedTables) {
        this.occupiedTables = occupiedTables;
    }

    public Integer getActiveOrders() {
        return activeOrders;
    }

    public void setActiveOrders(Integer activeOrders) {
        this.activeOrders = activeOrders;
    }

    public Integer getClosedOrdersToday() {
        return closedOrdersToday;
    }

    public void setClosedOrdersToday(Integer closedOrdersToday) {
        this.closedOrdersToday = closedOrdersToday;
    }

    public BigDecimal getSalesToday() {
        return salesToday;
    }

    public void setSalesToday(BigDecimal salesToday) {
        this.salesToday = salesToday;
    }

    public Integer getTotalTables() {
        return totalTables;
    }

    public void setTotalTables(Integer totalTables) {
        this.totalTables = totalTables;
    }

    public Integer getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(Integer lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DashboardReportDto dto = new DashboardReportDto();

        public Builder occupiedTables(Integer occupiedTables) {
            dto.occupiedTables = occupiedTables;
            return this;
        }

        public Builder activeOrders(Integer activeOrders) {
            dto.activeOrders = activeOrders;
            return this;
        }

        public Builder closedOrdersToday(Integer closedOrdersToday) {
            dto.closedOrdersToday = closedOrdersToday;
            return this;
        }

        public Builder salesToday(BigDecimal salesToday) {
            dto.salesToday = salesToday;
            return this;
        }

        public Builder totalTables(Integer totalTables) {
            dto.totalTables = totalTables;
            return this;
        }

        public Builder lowStockProducts(Integer lowStockProducts) {
            dto.lowStockProducts = lowStockProducts;
            return this;
        }

        public DashboardReportDto build() {
            return dto;
        }
    }
}
