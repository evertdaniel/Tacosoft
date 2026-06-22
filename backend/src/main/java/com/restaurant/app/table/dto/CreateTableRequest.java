package com.restaurant.app.table.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** DTO for creating a table. */
public class CreateTableRequest {
    @NotNull(message = "Table number is required") private Integer num;

    @NotNull(message = "Number of seats is required") @Min(value = 1, message = "Tables must have at least 1 seat")
    @Max(value = 20, message = "Tables cannot have more than 20 seats")
    private Integer seats;

    private Integer posX;
    private Integer posY;

    public CreateTableRequest() {}

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
}
