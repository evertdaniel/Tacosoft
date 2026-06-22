package com.restaurant.app.supplier.repository;

import com.restaurant.app.supplier.model.Supplier;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Supplier entity. Implements T10.1 with tenant filtering. */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {

    /** Find all suppliers for a specific restaurant. */
    @Query("SELECT s FROM Supplier s WHERE s.restaurantId = :restaurantId ORDER BY s.name")
    List<Supplier> findByRestaurantId(@Param("restaurantId") String restaurantId);

    /** Find active suppliers for a specific restaurant. */
    @Query(
            "SELECT s FROM Supplier s WHERE s.restaurantId = :restaurantId AND s.isActive ="
                    + " :isActive ORDER BY s.name")
    List<Supplier> findByRestaurantIdAndIsActive(
            @Param("restaurantId") String restaurantId, @Param("isActive") Boolean isActive);

    /** Find a supplier by ID and restaurant ID. */
    @Query("SELECT s FROM Supplier s WHERE s.id = :id AND s.restaurantId = :restaurantId")
    Optional<Supplier> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);

    /** Check if a supplier exists by ID and restaurant ID. */
    @Query(
            "SELECT COUNT(s) > 0 FROM Supplier s WHERE s.id = :id AND s.restaurantId ="
                    + " :restaurantId")
    boolean existsByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);

    /** Find suppliers by name pattern for a specific restaurant. */
    @Query(
            "SELECT s FROM Supplier s WHERE s.restaurantId = :restaurantId AND LOWER(s.name) LIKE"
                    + " LOWER(CONCAT('%', :name, '%')) ORDER BY s.name")
    List<Supplier> findByRestaurantIdAndNameContaining(
            @Param("restaurantId") String restaurantId, @Param("name") String name);
}
