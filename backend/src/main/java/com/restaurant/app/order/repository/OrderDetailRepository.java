package com.restaurant.app.order.repository;

import com.restaurant.app.order.model.OrderDetail;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for OrderDetail entity. */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {

    /** Find all details for an order. */
    List<OrderDetail> findByOrderId(@Param("orderId") String orderId);

    /** Find details by status. */
    List<OrderDetail> findByOrderIdAndStatus(
            @Param("orderId") String orderId, @Param("status") String status);

    /** Find detail by ID and restaurant ID (tenant-scoped). */
    Optional<OrderDetail> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);

    /** Find details by restaurant and order ID. */
    List<OrderDetail> findByRestaurantIdAndOrderId(
            @Param("restaurantId") String restaurantId, @Param("orderId") String orderId);
}
