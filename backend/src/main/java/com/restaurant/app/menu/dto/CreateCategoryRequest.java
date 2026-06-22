package com.restaurant.app.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO for creating a category. */
public class CreateCategoryRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotBlank(message = "Section ID is required")
    private String sectionId;

    public CreateCategoryRequest() {}

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

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }
}
