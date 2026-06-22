package com.restaurant.app.table.repository;

import com.restaurant.app.table.model.RestaurantTable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for RestaurantTable entity. */
@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, String> {

    /** Find all tables for a restaurant. */
    List<RestaurantTable> findByRestaurantId(@Param("restaurantId") String restaurantId);

    /** Find active tables for a restaurant. */
    List<RestaurantTable> findByRestaurantIdAndIsActive(
            @Param("restaurantId") String restaurantId, @Param("isActive") boolean isActive);

    /** Check if table exists by number and restaurant. */
    boolean existsByRestaurantIdAndNum(
            @Param("restaurantId") String restaurantId, @Param("num") Integer num);

    /** Find tables by status. */
    List<RestaurantTable> findByRestaurantIdAndStatus(
            @Param("restaurantId") String restaurantId, @Param("status") String status);

    /** Find table by number and restaurant. */
    Optional<RestaurantTable> findByNumAndRestaurantId(
            @Param("num") Integer num, @Param("restaurantId") String restaurantId);

    /** Find table by ID and restaurant ID (tenant-scoped). */
    Optional<RestaurantTable> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);

    /** Find available tables for a restaurant. */
    List<RestaurantTable> findByRestaurantIdAndStatusAndIsActiveTrue(
            @Param("restaurantId") String restaurantId, @Param("status") String status);
}
