package com.restaurant.app.report.dto;

import java.math.BigDecimal;

/** Top product in sales report. Implements SPEC-REPORT-001. */
public class TopProductDto {

    private String productId;
    private String productName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
    private Long orderCount;

    public TopProductDto() {}

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

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final TopProductDto dto = new TopProductDto();

        public Builder productId(String productId) {
            dto.productId = productId;
            return this;
        }

        public Builder productName(String productName) {
            dto.productName = productName;
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

        public Builder orderCount(Long orderCount) {
            dto.orderCount = orderCount;
            return this;
        }

        public TopProductDto build() {
            return dto;
        }
    }
}
