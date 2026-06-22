package com.restaurant.app.menu.repository;

import com.restaurant.app.menu.model.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Product entity. */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    /** Find all products for a restaurant. */
    List<Product> findByRestaurantId(@Param("restaurantId") String restaurantId);

    /** Find active products for a restaurant. */
    List<Product> findByRestaurantIdAndIsActive(
            @Param("restaurantId") String restaurantId, @Param("isActive") boolean isActive);

    /** Find products by category. */
    List<Product> findByCategoryId(@Param("categoryId") String categoryId);

    /** Find products by status. */
    List<Product> findByRestaurantIdAndStatus(
            @Param("restaurantId") String restaurantId, @Param("status") String status);

    /** Find product by ID and restaurant ID (tenant-scoped). */
    Optional<Product> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);

    /** Find products with low stock. */
    @Query(
            "SELECT p FROM Product p WHERE p.restaurantId = :restaurantId AND p.manageStock = true"
                    + " AND p.stock < 10")
    List<Product> findLowStockProducts(@Param("restaurantId") String restaurantId);

    /** Check if product exists by ID and restaurant. */
    boolean existsByIdAndRestaurantId(String id, String restaurantId);

    /** Find products by restaurant and category. */
    List<Product> findByRestaurantIdAndCategoryId(String restaurantId, String categoryId);

    /** Find active products by restaurant and status. */
    List<Product> findByRestaurantIdAndIsActiveAndStatus(
            String restaurantId, boolean isActive, String status);
}
