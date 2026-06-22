package com.restaurant.app.billing.model;

import com.restaurant.app.common.TenantAware;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Invoice entity - legal receipt with sequential folio per restaurant. Implements SPEC-BILL-001 and
 * INV-02 (unique folio per restaurant).
 */
@Entity
@Table(name = "invoice")
public class Invoice extends TenantAware {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "order_id", nullable = false, columnDefinition = "CHAR(36)")
    private String orderId;

    @Column(name = "folio", nullable = false)
    private Long folio;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax", nullable = false, precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private com.restaurant.app.order.model.Order order;

    public Invoice() {}

    public static Builder builder() {
        return new Builder();
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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public com.restaurant.app.order.model.Order getOrder() {
        return order;
    }

    public void setOrder(com.restaurant.app.order.model.Order order) {
        this.order = order;
    }

    public Long getFolio() {
        return folio;
    }

    public void setFolio(Long folio) {
        this.folio = folio;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @PrePersist
    protected void onCreate() {
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedAt(LocalDateTime.now());
    }

    public static class Builder {
        private final Invoice invoice = new Invoice();

        public Builder id(String id) {
            invoice.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            invoice.setRestaurantId(restaurantId);
            return this;
        }

        public Builder orderId(String orderId) {
            invoice.setOrderId(orderId);
            return this;
        }

        public Builder folio(Long folio) {
            invoice.setFolio(folio);
            return this;
        }

        public Builder subtotal(BigDecimal subtotal) {
            invoice.setSubtotal(subtotal);
            return this;
        }

        public Builder tax(BigDecimal tax) {
            invoice.setTax(tax);
            return this;
        }

        public Builder total(BigDecimal total) {
            invoice.setTotal(total);
            return this;
        }

        public Builder isPaid(Boolean isPaid) {
            invoice.setIsPaid(isPaid);
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            invoice.setPaymentMethod(paymentMethod);
            return this;
        }

        public Invoice build() {
            return invoice;
        }
    }
}
