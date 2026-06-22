package com.restaurant.app.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** OrderDetail entity - individual items within an order. */
@Entity
@Table(name = "order_detail")
public class OrderDetail {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "restaurant_id", nullable = false, columnDefinition = "CHAR(36)")
    private String restaurantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "order_id", nullable = false, columnDefinition = "CHAR(36)")
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private Order order;

    @Column(name = "product_id", nullable = false, columnDefinition = "CHAR(36)")
    private String productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private com.restaurant.app.menu.model.Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "product_option_id", columnDefinition = "CHAR(36)")
    private String productOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_option_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private com.restaurant.app.menu.model.ProductOption productOption;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
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

    // Default constructor
    public OrderDetail() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public com.restaurant.app.menu.model.Product getProduct() {
        return product;
    }

    public void setProduct(com.restaurant.app.menu.model.Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public com.restaurant.app.menu.model.ProductOption getProductOption() {
        return productOption;
    }

    public void setProductOption(com.restaurant.app.menu.model.ProductOption productOption) {
        this.productOption = productOption;
    }

    public static class Builder {
        private final OrderDetail detail = new OrderDetail();

        public Builder id(String id) {
            detail.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            detail.setRestaurantId(restaurantId);
            return this;
        }

        public Builder orderId(String orderId) {
            detail.orderId = orderId;
            return this;
        }

        public Builder productId(String productId) {
            detail.productId = productId;
            return this;
        }

        public Builder quantity(Integer quantity) {
            detail.quantity = quantity;
            return this;
        }

        public Builder price(BigDecimal price) {
            detail.price = price;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            detail.amount = amount;
            return this;
        }

        public Builder status(String status) {
            detail.status = status;
            return this;
        }

        public Builder notes(String notes) {
            detail.notes = notes;
            return this;
        }

        public Builder productOptionId(String productOptionId) {
            detail.productOptionId = productOptionId;
            return this;
        }

        public OrderDetail build() {
            return detail;
        }
    }
}
