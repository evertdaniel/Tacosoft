package com.restaurant.app.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** DTO for order responses. */
public class OrderDto {
    private String id;
    private Integer num;
    private String type;
    private String status;
    private BigDecimal total;
    private Integer people;
    private String tableId;
    private String clientId;
    private List<OrderDetailDto> details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderDto() {}

    public OrderDto(
            String id,
            Integer num,
            String type,
            String status,
            BigDecimal total,
            Integer people,
            String tableId,
            String clientId,
            List<OrderDetailDto> details,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.num = num;
        this.type = type;
        this.status = status;
        this.total = total;
        this.people = people;
        this.tableId = tableId;
        this.clientId = clientId;
        this.details = details;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
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

    public List<OrderDetailDto> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetailDto> details) {
        this.details = details;
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
