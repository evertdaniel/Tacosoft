package com.restaurant.app.auth.model;

import jakarta.persistence.*;

/** Role entity - RBAC role (ADMIN, COOK, WAITER, CASHIER). */
@Entity
@Table(name = "role")
public class Role {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    // Default constructor
    public Role() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public static class Builder {
        private final Role role = new Role();

        public Builder id(Integer id) {
            role.id = id;
            return this;
        }

        public Builder name(String name) {
            role.name = name;
            return this;
        }

        public Builder description(String description) {
            role.description = description;
            return this;
        }

        public Role build() {
            return role;
        }
    }
}
