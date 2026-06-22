package com.restaurant.app.common;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/** Base entity for multi-tenancy support. All business entities belong to a restaurant (tenant). */
@MappedSuperclass
public abstract class TenantAware {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    protected String id;

    @Column(name = "restaurant_id", nullable = false, columnDefinition = "CHAR(36)")
    private String restaurantId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
}
