package com.restaurant.app.auth.repository;

import com.restaurant.app.auth.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Role entity. */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /** Find role by name (for RBAC checks). */
    Optional<Role> findByName(String name);
}
