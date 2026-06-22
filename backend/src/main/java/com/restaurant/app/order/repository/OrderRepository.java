package com.restaurant.app.order.repository;

import com.restaurant.app.order.model.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Order entity. */
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    /** Find all orders for a restaurant. */
    List<Order> findByRestaurantId(@Param("restaurantId") String restaurantId);

    /** Find orders by status. */
    List<Order> findByRestaurantIdAndStatus(
            @Param("restaurantId") String restaurantId, @Param("status") String status);

    /** Find active orders (not closed/cancelled). */
    @Query(
            "SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.status NOT IN"
                    + " ('CLOSED', 'CANCELLED')")
    List<Order> findActiveOrders(@Param("restaurantId") String restaurantId);

    /** Find order by number and restaurant (INV-01: unique constraint). */
    Optional<Order> findByNumAndRestaurantId(
            @Param("num") Integer num, @Param("restaurantId") String restaurantId);

    /** Find order by ID and restaurant ID (tenant-scoped). */
    Optional<Order> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);

    /** Find active orders (not completed/cancelled). */
    @Query(
            "SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.status NOT IN"
                    + " :statuses")
    List<Order> findByRestaurantIdAndStatusNotIn(
            @Param("restaurantId") String restaurantId, @Param("statuses") List<String> statuses);

    /** Find max order number for restaurant. */
    @Query("SELECT MAX(o.num) FROM Order o WHERE o.restaurantId = :restaurantId")
    Integer findMaxNumByRestaurantId(@Param("restaurantId") String restaurantId);

    /** Find orders by date range. */
    @Query(
            "SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.createdAt BETWEEN"
                    + " :startDate AND :endDate")
    List<Order> findByRestaurantIdAndDateRange(
            @Param("restaurantId") String restaurantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /** Get max order number for a restaurant. */
    @Query("SELECT COALESCE(MAX(o.num), 0) FROM Order o WHERE o.restaurantId = :restaurantId")
    Integer getMaxOrderNum(@Param("restaurantId") String restaurantId);
}
