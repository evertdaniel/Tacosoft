package com.restaurant.app.menu.repository;

import com.restaurant.app.menu.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Category entity. */
@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    /** Find all categories for a restaurant. */
    List<Category> findByRestaurantId(@Param("restaurantId") String restaurantId);

    /** Find active categories for a restaurant. */
    List<Category> findByRestaurantIdAndIsActive(
            @Param("restaurantId") String restaurantId, @Param("isActive") boolean isActive);

    /** Find categories by section. */
    List<Category> findBySectionId(@Param("sectionId") String sectionId);

    /** Check if category exists by ID and restaurant. */
    boolean existsByIdAndRestaurantId(String id, String restaurantId);

    /** Find category by ID and restaurant ID (tenant-scoped). */
    Optional<Category> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);
}
