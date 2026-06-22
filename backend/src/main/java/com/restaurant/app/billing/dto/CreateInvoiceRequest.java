package com.restaurant.app.billing.dto;

import jakarta.validation.constraints.NotNull;

/** Request DTO for creating an invoice. */
public class CreateInvoiceRequest {

    @NotNull(message = "orderId is required") private String orderId;

    public CreateInvoiceRequest() {}

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CreateInvoiceRequest request = new CreateInvoiceRequest();

        public Builder orderId(String orderId) {
            request.setOrderId(orderId);
            return this;
        }

        public CreateInvoiceRequest build() {
            return request;
        }
    }
}
