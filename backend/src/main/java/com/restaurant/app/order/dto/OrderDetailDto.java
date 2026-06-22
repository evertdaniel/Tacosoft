package com.restaurant.app.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO for order detail responses. */
public class OrderDetailDto {
    private String id;
    private String orderId;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String status;
    private String notes;
    private String productOptionId;
    private String productOptionName;
    private BigDecimal priceAdjustment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderDetailDto() {}

    public OrderDetailDto(
            String id,
            String orderId,
            String productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal amount,
            String status,
            String notes,
            String productOptionId,
            String productOptionName,
            BigDecimal priceAdjustment,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.amount = amount;
        this.status = status;
        this.notes = notes;
        this.productOptionId = productOptionId;
        this.productOptionName = productOptionName;
        this.priceAdjustment = priceAdjustment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getProductOptionId() {
        return productOptionId;
    }

    public void setProductOptionId(String productOptionId) {
        this.productOptionId = productOptionId;
    }

    public String getProductOptionName() {
        return productOptionName;
    }

    public void setProductOptionName(String productOptionName) {
        this.productOptionName = productOptionName;
    }

    public BigDecimal getPriceAdjustment() {
        return priceAdjustment;
    }

    public void setPriceAdjustment(BigDecimal priceAdjustment) {
        this.priceAdjustment = priceAdjustment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
