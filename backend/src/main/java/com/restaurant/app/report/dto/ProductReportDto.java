package com.restaurant.app.report.dto;

import java.math.BigDecimal;

/** Product report with sales, margin analysis, and stock turnover. Implements SPEC-REPORT-001. */
public class ProductReportDto {

    private String productId;
    private String productName;
    private Long orderCount;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
    private BigDecimal unitCost;
    private BigDecimal totalMargin;
    private Double marginPercentage;
    private Integer currentStock;
    private Integer stockTurnoverDays;

    public ProductReportDto() {}

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalMargin() {
        return totalMargin;
    }

    public void setTotalMargin(BigDecimal totalMargin) {
        this.totalMargin = totalMargin;
    }

    public Double getMarginPercentage() {
        return marginPercentage;
    }

    public void setMarginPercentage(Double marginPercentage) {
        this.marginPercentage = marginPercentage;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getStockTurnoverDays() {
        return stockTurnoverDays;
    }

    public void setStockTurnoverDays(Integer stockTurnoverDays) {
        this.stockTurnoverDays = stockTurnoverDays;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ProductReportDto dto = new ProductReportDto();

        public Builder productId(String productId) {
            dto.productId = productId;
            return this;
        }

        public Builder productName(String productName) {
            dto.productName = productName;
            return this;
        }

        public Builder orderCount(Long orderCount) {
            dto.orderCount = orderCount;
            return this;
        }

        public Builder totalQuantity(Long totalQuantity) {
            dto.totalQuantity = totalQuantity;
            return this;
        }

        public Builder totalRevenue(BigDecimal totalRevenue) {
            dto.totalRevenue = totalRevenue;
            return this;
        }

        public Builder unitCost(BigDecimal unitCost) {
            dto.unitCost = unitCost;
            return this;
        }

        public Builder totalMargin(BigDecimal totalMargin) {
            dto.totalMargin = totalMargin;
            return this;
        }

        public Builder marginPercentage(Double marginPercentage) {
            dto.marginPercentage = marginPercentage;
            return this;
        }

        public Builder currentStock(Integer currentStock) {
            dto.currentStock = currentStock;
            return this;
        }

        public Builder stockTurnoverDays(Integer stockTurnoverDays) {
            dto.stockTurnoverDays = stockTurnoverDays;
            return this;
        }

        public ProductReportDto build() {
            return dto;
        }
    }
}
