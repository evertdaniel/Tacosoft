package com.restaurant.app.billing.repository;

import com.restaurant.app.billing.model.Bill;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Bill entities. All queries are tenant-scoped. */
@Repository
public interface BillRepository extends JpaRepository<Bill, String> {

    @Query("SELECT b FROM Bill b WHERE b.orderId = :orderId")
    Optional<Bill> findByOrderId(@Param("orderId") String orderId);

    @Query(
            "SELECT b FROM Bill b JOIN com.restaurant.app.order.model.Order o "
                    + "WHERE o.id = :orderId AND o.restaurantId = :restaurantId")
    Optional<Bill> findByOrderIdAndRestaurantId(
            @Param("orderId") String orderId, @Param("restaurantId") String restaurantId);

    @Query(
            "SELECT b FROM Bill b JOIN com.restaurant.app.order.model.Order o "
                    + "WHERE o.restaurantId = :restaurantId")
    List<Bill> findAllByRestaurantId(@Param("restaurantId") String restaurantId);

    @Query(
            "SELECT b FROM Bill b JOIN com.restaurant.app.order.model.Order o "
                    + "WHERE o.restaurantId = :restaurantId AND b.isPaid = :isPaid")
    List<Bill> findByRestaurantIdAndIsPaid(
            @Param("restaurantId") String restaurantId, @Param("isPaid") Boolean isPaid);
}
