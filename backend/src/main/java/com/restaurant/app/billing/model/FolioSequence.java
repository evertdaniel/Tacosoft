package com.restaurant.app.billing.model;

import com.restaurant.app.common.Auditable;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * FolioSequence entity - manages invoice folio sequence per restaurant. Implements INV-02
 * (contiguous folios with pessimistic locking).
 */
@Entity
@Table(name = "folio_sequence")
public class FolioSequence extends Auditable {

    @Id
    @Column(name = "restaurant_id", columnDefinition = "CHAR(36)")
    private String restaurantId;

    @Column(name = "next_folio", nullable = false)
    private Long nextFolio = 1L;

    // Restaurant reference via restaurantId only - no separate Restaurant entity

    public FolioSequence() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Long getNextFolio() {
        return nextFolio;
    }

    public void setNextFolio(Long nextFolio) {
        this.nextFolio = nextFolio;
    }

    /** Get current folio and increment atomically. Called within pessimistic lock transaction. */
    public Long getAndIncrement() {
        Long current = nextFolio;
        nextFolio++;
        return current;
    }

    @PrePersist
    protected void onCreate() {
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }

    @PreUpdate
    protected void onUpdate() {
        setUpdatedAt(LocalDateTime.now());
    }

    public static class Builder {
        private final FolioSequence folioSequence = new FolioSequence();

        public Builder restaurantId(String restaurantId) {
            folioSequence.setRestaurantId(restaurantId);
            return this;
        }

        public Builder nextFolio(Long nextFolio) {
            folioSequence.setNextFolio(nextFolio);
            return this;
        }

        public FolioSequence build() {
            return folioSequence;
        }
    }
}
