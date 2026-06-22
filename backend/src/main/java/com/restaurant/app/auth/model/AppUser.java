package com.restaurant.app.auth.model;

import com.restaurant.app.user.model.Person;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** AppUser entity - application user with authentication and RBAC. */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "primary_role_id")
    private Integer primaryRoleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "primary_role_id", insertable = false, updatable = false)
    private Role primaryRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", columnDefinition = "CHAR(36)")
    private Person person;

    @Column(
            name = "person_id",
            insertable = false,
            updatable = false,
            columnDefinition = "CHAR(36)")
    private String personId;

    @Column(name = "restaurant_id", columnDefinition = "CHAR(36)")
    private String restaurantId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRestaurantRole> restaurantRoles = new ArrayList<>();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Default constructor
    public AppUser() {}

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getPrimaryRoleId() {
        return primaryRoleId;
    }

    public void setPrimaryRoleId(Integer primaryRoleId) {
        this.primaryRoleId = primaryRoleId;
    }

    public Role getPrimaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(Role primaryRole) {
        this.primaryRole = primaryRole;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public List<UserRestaurantRole> getRestaurantRoles() {
        return restaurantRoles;
    }

    public void setRestaurantRoles(List<UserRestaurantRole> restaurantRoles) {
        this.restaurantRoles = restaurantRoles;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public static class Builder {
        private final AppUser user = new AppUser();

        public Builder id(String id) {
            user.setId(id);
            return this;
        }

        public Builder username(String username) {
            user.username = username;
            return this;
        }

        public Builder password(String password) {
            user.password = password;
            return this;
        }

        public Builder active(boolean active) {
            user.active = active;
            return this;
        }

        public Builder primaryRoleId(Integer primaryRoleId) {
            user.primaryRoleId = primaryRoleId;
            return this;
        }

        public Builder primaryRole(Role primaryRole) {
            user.primaryRole = primaryRole;
            return this;
        }

        public Builder person(Person person) {
            user.person = person;
            return this;
        }

        public Builder restaurantRoles(List<UserRestaurantRole> restaurantRoles) {
            user.restaurantRoles = restaurantRoles;
            return this;
        }

        public Builder lastLogin(LocalDateTime lastLogin) {
            user.lastLogin = lastLogin;
            return this;
        }

        public AppUser build() {
            return user;
        }
    }
}
