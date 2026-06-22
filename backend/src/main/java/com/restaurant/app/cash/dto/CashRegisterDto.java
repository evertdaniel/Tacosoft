package com.restaurant.app.cash.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** CashRegister DTO - API contract for cash registers. */
public class CashRegisterDto {

    private String id;
    private String restaurantId;
    private String userId;
    private BigDecimal openingAmount;
    private BigDecimal closingAmount;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime openedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime closedAt;

    public CashRegisterDto() {}

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CashRegisterDto dto = new CashRegisterDto();

        public Builder id(String id) {
            dto.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            dto.setRestaurantId(restaurantId);
            return this;
        }

        public Builder userId(String userId) {
            dto.setUserId(userId);
            return this;
        }

        public Builder openingAmount(BigDecimal openingAmount) {
            dto.setOpeningAmount(openingAmount);
            return this;
        }

        public Builder closingAmount(BigDecimal closingAmount) {
            dto.setClosingAmount(closingAmount);
            return this;
        }

        public Builder status(String status) {
            dto.setStatus(status);
            return this;
        }

        public Builder openedAt(LocalDateTime openedAt) {
            dto.setOpenedAt(openedAt);
            return this;
        }

        public Builder closedAt(LocalDateTime closedAt) {
            dto.setClosedAt(closedAt);
            return this;
        }

        public CashRegisterDto build() {
            return dto;
        }
    }
}
