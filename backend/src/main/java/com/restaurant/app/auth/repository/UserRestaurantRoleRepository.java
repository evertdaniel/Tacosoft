package com.restaurant.app.auth.repository;

import com.restaurant.app.auth.model.UserRestaurantRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for UserRestaurantRole junction entity. */
@Repository
public interface UserRestaurantRoleRepository extends JpaRepository<UserRestaurantRole, String> {

    /** Find all restaurant roles for a user. */
    List<UserRestaurantRole> findByUserId(String userId);

    /** Find user's role in a specific restaurant. */
    @Query(
            "SELECT urr FROM UserRestaurantRole urr WHERE urr.userId = :userId AND urr.restaurantId"
                    + " = :restaurantId")
    java.util.Optional<UserRestaurantRole> findByUserAndRestaurant(
            @Param("userId") String userId, @Param("restaurantId") String restaurantId);

    /** Find all users with a specific role in a restaurant. */
    List<UserRestaurantRole> findByRestaurantIdAndRoleId(String restaurantId, Integer roleId);

    /** Check if user has specific role in restaurant. */
    @Query(
            "SELECT CASE WHEN COUNT(urr) > 0 THEN true ELSE false END FROM UserRestaurantRole urr"
                + " WHERE urr.userId = :userId AND urr.restaurantId = :restaurantId AND urr.roleId"
                + " = :roleId")
    boolean existsByUserIdAndRestaurantIdAndRoleId(
            @Param("userId") String userId,
            @Param("restaurantId") String restaurantId,
            @Param("roleId") Integer roleId);

    /** Find user restaurant role by user, restaurant, and role. */
    @Query(
            "SELECT urr FROM UserRestaurantRole urr WHERE urr.userId = :userId AND urr.restaurantId"
                    + " = :restaurantId AND urr.roleId = :roleId")
    java.util.Optional<UserRestaurantRole> findByUserIdAndRestaurantIdAndRoleId(
            @Param("userId") String userId,
            @Param("restaurantId") String restaurantId,
            @Param("roleId") Integer roleId);
}
