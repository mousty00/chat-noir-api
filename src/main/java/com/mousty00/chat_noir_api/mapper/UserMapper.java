package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.dto.user.UserDTO;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.generic.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserRoleMapper.class, SubscriptionPlanMapper.class})
public interface UserMapper extends GenericMapper<User, UserDTO> {

    @Override
    @Mapping(target = "image", ignore = true)
    UserDTO toDTO(User user);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isAdmin", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "imageKey", ignore = true)
    @Mapping(target = "stripeSubscriptionId", ignore = true)
    User toEntity(UserDTO userDTO);




}