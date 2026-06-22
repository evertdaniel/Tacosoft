package com.restaurant.app.billing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Invoice DTO - API contract for invoices. */
public class InvoiceDto {

    private String id;
    private String restaurantId;
    private String orderId;
    private Long folio;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    private Boolean isPaid;
    private String paymentMethod;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public InvoiceDto() {}

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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final InvoiceDto dto = new InvoiceDto();

        public Builder id(String id) {
            dto.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            dto.setRestaurantId(restaurantId);
            return this;
        }

        public Builder orderId(String orderId) {
            dto.setOrderId(orderId);
            return this;
        }

        public Builder folio(Long folio) {
            dto.setFolio(folio);
            return this;
        }

        public Builder subtotal(BigDecimal subtotal) {
            dto.setSubtotal(subtotal);
            return this;
        }

        public Builder tax(BigDecimal tax) {
            dto.setTax(tax);
            return this;
        }

        public Builder total(BigDecimal total) {
            dto.setTotal(total);
            return this;
        }

        public Builder isPaid(Boolean isPaid) {
            dto.setIsPaid(isPaid);
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            dto.setPaymentMethod(paymentMethod);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            dto.setCreatedAt(createdAt);
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            dto.setUpdatedAt(updatedAt);
            return this;
        }

        public InvoiceDto build() {
            return dto;
        }
    }
}
