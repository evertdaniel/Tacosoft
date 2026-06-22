package com.restaurant.app.security;

import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** UserDetailsService implementation for JWT authentication. */
@Service
public class UserDetailsServiceAdapter implements UserDetailsService {

    private final AppUserRepository userRepository;

    public UserDetailsServiceAdapter(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new UsernameNotFoundException("User not found: " + username));

        // Load with roles
        AppUser userWithRoles =
                userRepository
                        .findByIdWithRoles(user.getId())
                        .orElseThrow(
                                () -> new UsernameNotFoundException("User not found: " + username));

        return new UserDetailsAdapter(
                userWithRoles.getId(),
                userWithRoles.getUsername(),
                userWithRoles.getPassword(),
                userWithRoles.isActive(),
                userWithRoles.getRestaurantRoles(),
                userWithRoles.getPrimaryRole());
    }
}
