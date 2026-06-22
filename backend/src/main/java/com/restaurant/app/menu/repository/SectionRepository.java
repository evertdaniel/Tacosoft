package com.restaurant.app.menu.repository;

import com.restaurant.app.menu.model.Section;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Section entity. */
@Repository
public interface SectionRepository extends JpaRepository<Section, String> {

    /** Find all sections for a restaurant, ordered by display_order. */
    List<Section> findByRestaurantIdOrderByDisplayOrderAsc(
            @Param("restaurantId") String restaurantId);

    /** Find active sections for a restaurant. */
    List<Section> findByRestaurantIdAndIsActiveTrueOrderByDisplayOrderAsc(
            @Param("restaurantId") String restaurantId);

    /** Find section by ID and restaurant ID (tenant-scoped). */
    List<Section> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);

    /** Check if section exists by ID and restaurant. */
    boolean existsByIdAndRestaurantId(String id, String restaurantId);
}
