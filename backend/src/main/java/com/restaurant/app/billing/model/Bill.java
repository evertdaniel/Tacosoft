package com.restaurant.app.billing.model;

import com.restaurant.app.common.Auditable;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Bill entity - links orders to payment status. Implements SPEC-BILL-001. */
@Entity
@Table(name = "bill")
public class Bill extends Auditable {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "order_id", nullable = false, columnDefinition = "CHAR(36)")
    private String orderId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private com.restaurant.app.order.model.Order order;

    public Bill() {}

    public static Builder builder() {
        return new Builder();
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

    public com.restaurant.app.order.model.Order getOrder() {
        return order;
    }

    public void setOrder(com.restaurant.app.order.model.Order order) {
        this.order = order;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
        private final Bill bill = new Bill();

        public Builder id(String id) {
            bill.setId(id);
            return this;
        }

        public Builder orderId(String orderId) {
            bill.setOrderId(orderId);
            return this;
        }

        public Builder amount(BigDecimal amount) {
            bill.setAmount(amount);
            return this;
        }

        public Builder isPaid(Boolean isPaid) {
            bill.setIsPaid(isPaid);
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            bill.setPaymentMethod(paymentMethod);
            return this;
        }

        public Bill build() {
            return bill;
        }
    }
}
