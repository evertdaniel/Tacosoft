package com.restaurant.app.table.dto;

import java.time.LocalDateTime;

/** DTO for table responses. */
public class TableDto {
    private String id;
    private Integer num;
    private Integer seats;
    private String status;
    private Integer posX;
    private Integer posY;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TableDto() {}

    public TableDto(
            String id,
            Integer num,
            Integer seats,
            String status,
            Integer posX,
            Integer posY,
            boolean isActive,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.num = num;
        this.seats = seats;
        this.status = status;
        this.posX = posX;
        this.posY = posY;
        this.isActive = isActive;
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
