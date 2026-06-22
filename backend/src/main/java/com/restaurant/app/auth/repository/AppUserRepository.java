package com.restaurant.app.auth.repository;

import com.restaurant.app.auth.model.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for AppUser entity. */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, String> {

    /** Find user by username (for login). */
    Optional<AppUser> findByUsername(String username);

    /** Find user by ID with restaurant roles eager-loaded. */
    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.restaurantRoles WHERE u.id = :id")
    Optional<AppUser> findByIdWithRoles(@Param("id") String id);

    /** Find user by ID with person eager-loaded. */
    @Query(
            "SELECT u FROM AppUser u LEFT JOIN FETCH u.person LEFT JOIN FETCH u.primaryRole WHERE"
                    + " u.id = :id")
    Optional<AppUser> findByIdWithPerson(@Param("id") String id);

    /** Find users by restaurant ID. */
    @Query("SELECT u FROM AppUser u WHERE u.restaurantId = :restaurantId")
    java.util.List<AppUser> findByRestaurantId(@Param("restaurantId") String restaurantId);

    /** Find user by ID and restaurant ID (tenant-scoped). */
    @Query("SELECT u FROM AppUser u WHERE u.id = :id AND u.restaurantId = :restaurantId")
    Optional<AppUser> findByIdAndRestaurantId(
            @Param("id") String id, @Param("restaurantId") String restaurantId);
}
