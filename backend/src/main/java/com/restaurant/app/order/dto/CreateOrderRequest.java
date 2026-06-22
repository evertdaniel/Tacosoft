package com.restaurant.app.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/** DTO for creating an order. */
public class CreateOrderRequest {
    @NotNull(message = "Order type is required") @Pattern(regexp = "IN_PLACE|TAKE_AWAY", message = "Type must be IN_PLACE or TAKE_AWAY")
    private String type;

    @Min(value = 1, message = "At least 1 person is required")
    private Integer people;

    private String tableId;

    private String clientId;

    @NotEmpty(message = "Order must have at least one detail")
    private List<CreateOrderDetailRequest> details;

    public CreateOrderRequest() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPeople() {
        return people;
    }

    public void setPeople(Integer people) {
        this.people = people;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<CreateOrderDetailRequest> getDetails() {
        return details;
    }

    public void setDetails(List<CreateOrderDetailRequest> details) {
        this.details = details;
    }
}
