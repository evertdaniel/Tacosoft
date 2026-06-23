package com.restaurant.app.billing.repository;

import com.restaurant.app.billing.model.Invoice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Invoice entities. All queries are tenant-scoped. Enforces UNIQUE (restaurant_id,
 * folio) per INV-02.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {

    @Query("SELECT i FROM Invoice i WHERE i.id = :id AND i.restaurantId = :restaurantId")
    Optional<Invoice> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);

    @Query("SELECT i FROM Invoice i WHERE i.orderId = :orderId AND i.restaurantId = :restaurantId")
    Optional<Invoice> findByOrderIdAndRestaurantId(
            @Param("orderId") String orderId, @Param("restaurantId") String restaurantId);

    @Query("SELECT i FROM Invoice i WHERE i.restaurantId = :restaurantId")
    List<Invoice> findAllByRestaurantId(@Param("restaurantId") String restaurantId);

    @Query("SELECT i FROM Invoice i WHERE i.restaurantId = :restaurantId AND i.folio = :folio")
    Optional<Invoice> findByRestaurantIdAndFolio(
            @Param("restaurantId") String restaurantId, @Param("folio") Long folio);

    @Query("SELECT i FROM Invoice i WHERE i.restaurantId = :restaurantId AND i.isPaid = :isPaid")
    List<Invoice> findByRestaurantIdAndIsPaid(
            @Param("restaurantId") String restaurantId, @Param("isPaid") Boolean isPaid);

    @Query("SELECT i FROM Invoice i WHERE i.restaurantId = :restaurantId ORDER BY i.folio DESC")
    List<Invoice> findAllByRestaurantIdOrderByFolioDesc(@Param("restaurantId") String restaurantId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.restaurantId = :restaurantId")
    long countByRestaurantId(@Param("restaurantId") String restaurantId);

    @Query(
            "SELECT COUNT(i) FROM Invoice i WHERE i.orderId = :orderId AND i.restaurantId ="
                    + " :restaurantId AND i.isPaid = false")
    long countUnpaidByOrderIdAndRestaurantId(
            @Param("orderId") String orderId, @Param("restaurantId") String restaurantId);
}
