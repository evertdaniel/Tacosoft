package com.restaurant.app.cash.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CashRegister entity - tracks cash drawer openings and closings. Implements SPEC-CASH-001 and
 * INV-05.
 */
@Entity
@Table(name = "cash_register")
public class CashRegister {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "restaurant_id", nullable = false, columnDefinition = "CHAR(36)")
    private String restaurantId;

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "opening_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal openingAmount = BigDecimal.ZERO;

    @Column(name = "closing_amount", precision = 12, scale = 2)
    private BigDecimal closingAmount;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "OPEN"; // OPEN | CLOSED

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Restaurant and user references via IDs only - no separate entities

    public CashRegister() {}

    public static Builder builder() {
        return new Builder();
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getOpeningAmount() {
        return openingAmount;
    }

    public void setOpeningAmount(BigDecimal openingAmount) {
        this.openingAmount = openingAmount;
    }

    public BigDecimal getClosingAmount() {
        return closingAmount;
    }

    public void setClosingAmount(BigDecimal closingAmount) {
        this.closingAmount = closingAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public boolean isOpen() {
        return "OPEN".equals(status);
    }

    public boolean isClosed() {
        return "CLOSED".equals(status);
    }

    @PrePersist
    protected void onCreate() {
        if (openedAt == null) {
            openedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {}

    public static class Builder {
        private final CashRegister cashRegister = new CashRegister();

        public Builder id(String id) {
            cashRegister.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            cashRegister.setRestaurantId(restaurantId);
            return this;
        }

        public Builder userId(String userId) {
            cashRegister.setUserId(userId);
            return this;
        }

        public Builder openingAmount(BigDecimal openingAmount) {
            cashRegister.setOpeningAmount(openingAmount);
            return this;
        }

        public Builder closingAmount(BigDecimal closingAmount) {
            cashRegister.setClosingAmount(closingAmount);
            return this;
        }

        public Builder status(String status) {
            cashRegister.setStatus(status);
            return this;
        }

        public Builder openedAt(LocalDateTime openedAt) {
            cashRegister.setOpenedAt(openedAt);
            return this;
        }

        public Builder closedAt(LocalDateTime closedAt) {
            cashRegister.setClosedAt(closedAt);
            return this;
        }

        public CashRegister build() {
            return cashRegister;
        }
    }
}
