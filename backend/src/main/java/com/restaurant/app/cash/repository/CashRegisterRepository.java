package com.restaurant.app.cash.repository;

import com.restaurant.app.cash.model.CashRegister;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for CashRegister entities. All queries are tenant-scoped. */
@Repository
public interface CashRegisterRepository extends JpaRepository<CashRegister, String> {

    @Query("SELECT cr FROM CashRegister cr WHERE cr.id = :id AND cr.restaurantId = :restaurantId")
    Optional<CashRegister> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);

    @Query(
            "SELECT cr FROM CashRegister cr WHERE cr.userId = :userId AND cr.restaurantId ="
                    + " :restaurantId AND cr.status = 'OPEN'")
    Optional<CashRegister> findOpenByUserIdAndRestaurantId(
            @Param("userId") String userId, @Param("restaurantId") String restaurantId);

    @Query(
            "SELECT cr FROM CashRegister cr "
                    + "WHERE cr.restaurantId = :restaurantId AND cr.status = 'OPEN'")
    List<CashRegister> findAllOpenByRestaurantId(@Param("restaurantId") String restaurantId);

    @Query(
            "SELECT cr FROM CashRegister cr "
                    + "WHERE cr.restaurantId = :restaurantId AND cr.status = 'CLOSED' "
                    + "ORDER BY cr.closedAt DESC")
    List<CashRegister> findClosedByRestaurantIdOrderByClosedAtDesc(
            @Param("restaurantId") String restaurantId);

    @Query("SELECT cr FROM CashRegister cr WHERE cr.restaurantId = :restaurantId")
    List<CashRegister> findAllByRestaurantId(@Param("restaurantId") String restaurantId);

    @Query(
            "SELECT COUNT(cr) FROM CashRegister cr WHERE cr.userId = :userId AND cr.restaurantId ="
                    + " :restaurantId AND cr.status = 'OPEN'")
    long countOpenByUserIdAndRestaurantId(
            @Param("userId") String userId, @Param("restaurantId") String restaurantId);
}
