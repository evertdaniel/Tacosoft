package com.restaurant.app.user.dto;

import jakarta.validation.constraints.Size;

/** DTO for updating a user. */
public class UpdateUserRequest {
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    private Integer primaryRoleId;
    private Boolean active;
    private String personId;

    public UpdateUserRequest() {}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPrimaryRoleId() {
        return primaryRoleId;
    }

    public void setPrimaryRoleId(Integer primaryRoleId) {
        this.primaryRoleId = primaryRoleId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }
}
