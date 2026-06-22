package com.restaurant.app.menu.mapper;

import com.restaurant.app.menu.dto.SectionDto;
import com.restaurant.app.menu.model.Section;
import org.mapstruct.Mapper;

/** MapStruct mapper for Section entity. */
@Mapper(componentModel = "spring")
public interface SectionMapper {

    SectionDto toDto(Section section);
}
