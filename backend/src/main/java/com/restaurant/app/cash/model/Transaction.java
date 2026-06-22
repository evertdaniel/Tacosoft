package com.restaurant.app.cash.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity - records income/expense for cash registers. Implements SPEC-CASH-001 and
 * INV-03 (idempotent payments via UNIQUE reference_id).
 */
@Entity
@Table(
        name = "transaction",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_tx_reference", columnNames = "reference_id")
        })
public class Transaction {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "cash_register_id", nullable = false, columnDefinition = "CHAR(36)")
    private String cashRegisterId;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // INCOME | EXPENSE

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "reference_id", unique = true, columnDefinition = "CHAR(36)")
    private String referenceId; // Invoice/bill ID for idempotency (INV-03)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_register_id", insertable = false, updatable = false)
    private CashRegister cashRegister;

    public Transaction() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCashRegisterId() {
        return cashRegisterId;
    }

    public void setCashRegisterId(String cashRegisterId) {
        this.cashRegisterId = cashRegisterId;
    }

    public CashRegister getCashRegister() {
        return cashRegister;
    }

    public void setCashRegister(CashRegister cashRegister) {
        this.cashRegister = cashRegister;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isIncome() {
        return "INCOME".equals(type);
    }

    public boolean isExpense() {
        return "EXPENSE".equals(type);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {}

    public static class Builder {
        private final Transaction transaction = new Transaction();

        public Builder id(String id) {
            transaction.setId(id);
            return this;
        }

        public Builder cashRegisterId(String cashRegisterId) {
            transaction.setCashRegisterId(cashRegisterId);
            return this;
        }

        public Builder type(String type) {
            transaction.setType(type);
            return this;
        }

        public Builder amount(BigDecimal amount) {
            transaction.setAmount(amount);
            return this;
        }

        public Builder description(String description) {
            transaction.setDescription(description);
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            transaction.setPaymentMethod(paymentMethod);
            return this;
        }

        public Builder referenceId(String referenceId) {
            transaction.setReferenceId(referenceId);
            return this;
        }

        public Transaction build() {
            return transaction;
        }
    }
}
