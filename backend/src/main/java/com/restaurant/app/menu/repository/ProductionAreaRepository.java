package com.restaurant.app.menu.repository;

import com.restaurant.app.menu.model.ProductionArea;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for ProductionArea entity. */
@Repository
public interface ProductionAreaRepository extends JpaRepository<ProductionArea, String> {

    /** Find production area by name. */
    Optional<ProductionArea> findByName(String name);

    /** Find all areas for a restaurant. */
    List<ProductionArea> findByRestaurantId(String restaurantId);

    /** Check if production area exists by ID and restaurant. */
    boolean existsByIdAndRestaurantId(String id, String restaurantId);
}
