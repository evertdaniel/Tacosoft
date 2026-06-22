package com.restaurant.app.menu.repository;

import com.restaurant.app.menu.model.ProductOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for ProductOption entity. */
@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, String> {

    /** Find all options for a product. */
    List<ProductOption> findByProductId(@Param("productId") String productId);

    /** Find all options for a restaurant. */
    List<ProductOption> findByRestaurantId(@Param("restaurantId") String restaurantId);

    /** Find options by restaurant and product. */
    List<ProductOption> findByRestaurantIdAndProductId(
            @Param("restaurantId") String restaurantId, @Param("productId") String productId);

    /** Find available options for a product. */
    List<ProductOption> findByProductIdAndIsAvailableTrue(@Param("productId") String productId);

    /** Find default option for a product. */
    Optional<ProductOption> findByProductIdAndIsDefaultTrue(@Param("productId") String productId);

    /** Find available options by restaurant and product. */
    List<ProductOption> findByRestaurantIdAndProductIdAndIsAvailable(
            String restaurantId, String productId, boolean isAvailable);

    /** Find option by ID and restaurant ID (tenant-scoped). */
    Optional<ProductOption> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);
}
