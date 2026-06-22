package com.restaurant.app.auth.mapper;

import com.restaurant.app.auth.dto.RoleDto;
import com.restaurant.app.auth.model.Role;
import org.mapstruct.Mapper;

/** MapStruct mapper for Role entity. */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDto toDto(Role role);
}
