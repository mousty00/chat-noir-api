package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.dto.user.role.UserRoleDTO;
import com.mousty00.chat_noir_api.entity.UserRole;
import com.mousty00.chat_noir_api.util.generic.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserRoleMapper extends GenericMapper<UserRole, UserRoleDTO> {

    @Override
    UserRoleDTO toDTO(UserRole userRole);

    @Override
    @Mapping(target = "id", ignore = true)
    UserRole toEntity(UserRoleDTO userRoleDTO);
}