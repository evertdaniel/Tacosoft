package com.restaurant.app.auth.model;

import com.restaurant.app.common.TenantAware;
import jakarta.persistence.*;

/** UserRestaurantRole entity - junction table for user-role-restaurant assignments. */
@Entity
@Table(name = "user_restaurant_role")
public class UserRestaurantRole extends TenantAware {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private AppUser user;

    @Column(name = "user_id", insertable = false, updatable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "restaurant_id", nullable = false, columnDefinition = "CHAR(36)")
    private String restaurantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "role_id", insertable = false, updatable = false)
    private Integer roleId;

    // Default constructor
    public UserRestaurantRole() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public static class Builder {
        private final UserRestaurantRole junction = new UserRestaurantRole();

        public Builder id(String id) {
            junction.setId(id);
            return this;
        }

        public Builder user(AppUser user) {
            junction.user = user;
            return this;
        }

        public Builder userId(String userId) {
            junction.userId = userId;
            return this;
        }

        public Builder restaurantId(String restaurantId) {
            junction.restaurantId = restaurantId;
            return this;
        }

        public Builder role(Role role) {
            junction.role = role;
            return this;
        }

        public Builder roleId(Integer roleId) {
            junction.roleId = roleId;
            return this;
        }

        public UserRestaurantRole build() {
            return junction;
        }
    }
}
