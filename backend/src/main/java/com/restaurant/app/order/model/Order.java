package com.restaurant.app.order.model;

import com.restaurant.app.common.TenantAware;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** Order entity - customer orders. */
@Entity
@Table(name = "order")
@EntityListeners(AuditingEntityListener.class)
public class Order extends TenantAware {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "num", nullable = false)
    private Integer num;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal total = java.math.BigDecimal.ZERO;

    @Column(name = "people")
    private Integer people;

    @Column(name = "table_id", columnDefinition = "CHAR(36)")
    private String tableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "table_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private com.restaurant.app.table.model.RestaurantTable table;

    @Column(name = "client_id", columnDefinition = "CHAR(36)")
    private String clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "client_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private com.restaurant.app.user.model.Person client;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> details = new ArrayList<>();

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false;

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "notes", length = 500)
    private String notes;

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
    public Order() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
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

    public java.math.BigDecimal getTotal() {
        return total;
    }

    public void setTotal(java.math.BigDecimal total) {
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

    public com.restaurant.app.table.model.RestaurantTable getTable() {
        return table;
    }

    public void setTable(com.restaurant.app.table.model.RestaurantTable table) {
        this.table = table;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public com.restaurant.app.user.model.Person getClient() {
        return client;
    }

    public void setClient(com.restaurant.app.user.model.Person client) {
        this.client = client;
    }

    public List<OrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetail> details) {
        this.details = details;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public Boolean getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(Boolean isClosed) {
        this.isClosed = isClosed;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public static class Builder {
        private final Order order = new Order();

        public Builder id(String id) {
            order.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            order.setRestaurantId(restaurantId);
            return this;
        }

        public Builder num(Integer num) {
            order.num = num;
            return this;
        }

        public Builder type(String type) {
            order.type = type;
            return this;
        }

        public Builder status(String status) {
            order.status = status;
            return this;
        }

        public Builder total(java.math.BigDecimal total) {
            order.total = total;
            return this;
        }

        public Builder people(Integer people) {
            order.people = people;
            return this;
        }

        public Builder tableId(String tableId) {
            order.tableId = tableId;
            return this;
        }

        public Builder userId(String userId) {
            order.userId = userId;
            return this;
        }

        public Builder clientId(String clientId) {
            order.clientId = clientId;
            return this;
        }

        public Order build() {
            return order;
        }
    }
}
