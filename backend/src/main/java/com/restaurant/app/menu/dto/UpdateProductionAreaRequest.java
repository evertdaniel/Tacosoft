package com.restaurant.app.menu.dto;

import jakarta.validation.constraints.Size;

/** DTO for updating a production area. */
public class UpdateProductionAreaRequest {
    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    public UpdateProductionAreaRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
