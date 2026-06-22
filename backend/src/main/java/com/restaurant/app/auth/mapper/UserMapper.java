package com.restaurant.app.auth.mapper;

import com.restaurant.app.auth.dto.RestaurantRoleDto;
import com.restaurant.app.auth.dto.RoleDto;
import com.restaurant.app.auth.dto.UserDto;
import com.restaurant.app.auth.model.AppUser;
import com.restaurant.app.auth.model.UserRestaurantRole;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** MapStruct mapper for AppUser entity. */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "person.firstName", target = "firstName")
    @Mapping(source = "person.lastName", target = "lastName")
    @Mapping(source = "person.email", target = "email")
    @Mapping(source = "primaryRole", target = "primaryRole")
    @Mapping(
            source = "restaurantRoles",
            target = "restaurantRoles",
            qualifiedByName = "mapRestaurantRoles")
    UserDto toDto(AppUser user);

    @Named("mapRestaurantRoles")
    static List<RestaurantRoleDto> mapRestaurantRoles(List<UserRestaurantRole> restaurantRoles) {
        if (restaurantRoles == null) {
            return List.of();
        }
        return restaurantRoles.stream()
                .map(
                        urr ->
                                new RestaurantRoleDto(
                                        urr.getRestaurantId(),
                                        null, // Restaurant name would need to be loaded separately
                                        // if needed
                                        new RoleDto(
                                                urr.getRole().getId(),
                                                urr.getRole().getName(),
                                                urr.getRole().getDescription())))
                .collect(Collectors.toList());
    }
}
