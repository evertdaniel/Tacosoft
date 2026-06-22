package com.restaurant.app.table.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** RestaurantTable entity - dining tables in the restaurant. */
@Entity
@Table(name = "restaurant_table")
public class RestaurantTable {

    @Column(name = "num", nullable = false)
    private Integer num;

    @Column(name = "seats", nullable = false)
    private Integer seats;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "AVAILABLE";

    @Column(name = "pos_x")
    private Integer posX;

    @Column(name = "pos_y")
    private Integer posY;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "restaurant_id", nullable = false, columnDefinition = "CHAR(36)")
    private String restaurantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    // Default constructor
    public RestaurantTable() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPosX() {
        return posX;
    }

    public void setPosX(Integer posX) {
        this.posX = posX;
    }

    public Integer getPosY() {
        return posY;
    }

    public void setPosY(Integer posY) {
        this.posY = posY;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
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

    public static class Builder {
        private final RestaurantTable table = new RestaurantTable();

        public Builder id(String id) {
            table.setId(id);
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            table.setRestaurantId(restaurantId);
            return this;
        }

        public Builder num(Integer num) {
            table.num = num;
            return this;
        }

        public Builder seats(Integer seats) {
            table.seats = seats;
            return this;
        }

        public Builder status(String status) {
            table.status = status;
            return this;
        }

        public Builder posX(Integer posX) {
            table.posX = posX;
            return this;
        }

        public Builder posY(Integer posY) {
            table.posY = posY;
            return this;
        }

        public Builder isActive(boolean active) {
            table.isActive = active;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            table.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            table.updatedAt = updatedAt;
            return this;
        }

        public RestaurantTable build() {
            return table;
        }
    }
}
