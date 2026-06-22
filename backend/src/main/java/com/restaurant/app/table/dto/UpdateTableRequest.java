package com.restaurant.app.table.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** DTO for updating a table. */
public class UpdateTableRequest {
    private Integer num;

    @Min(value = 1, message = "Tables must have at least 1 seat")
    @Max(value = 20, message = "Tables cannot have more than 20 seats")
    private Integer seats;

    private Integer posX;
    private Integer posY;
    private Boolean isActive;

    public UpdateTableRequest() {}

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

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
