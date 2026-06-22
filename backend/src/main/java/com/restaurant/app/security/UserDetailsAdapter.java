package com.restaurant.app.security;

import com.restaurant.app.auth.model.Role;
import com.restaurant.app.auth.model.UserRestaurantRole;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Adapter between JPA AppUser and Spring Security UserDetails. */
public class UserDetailsAdapter implements UserDetails {

    private final String id;
    private final String username;
    private final String password;
    private final boolean active;
    private final List<UserRestaurantRole> restaurantRoles;
    private final Role primaryRole;

    public UserDetailsAdapter(
            String id,
            String username,
            String password,
            boolean active,
            List<UserRestaurantRole> restaurantRoles,
            Role primaryRole) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.active = active;
        this.restaurantRoles = restaurantRoles;
        this.primaryRole = primaryRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return primary role as authority
        return List.of(new SimpleGrantedAuthority("ROLE_" + primaryRole.getName()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public String getId() {
        return id;
    }

    public List<UserRestaurantRole> getRestaurantRoles() {
        return restaurantRoles;
    }

    public Role getPrimaryRole() {
        return primaryRole;
    }

    /** Check if user has a specific role in a restaurant. */
    public boolean hasRoleInRestaurant(String restaurantId, String roleName) {
        return restaurantRoles.stream()
                .anyMatch(
                        urr ->
                                urr.getRestaurantId().equals(restaurantId)
                                        && urr.getRole().getName().equals(roleName));
    }

    /** Check if user has ANY role in a restaurant. */
    public boolean hasAnyRoleInRestaurant(String restaurantId) {
        return restaurantRoles.stream().anyMatch(urr -> urr.getRestaurantId().equals(restaurantId));
    }
}
