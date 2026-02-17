package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.aws.S3Service;
import com.mousty00.chat_noir_api.dto.user.UserDTO;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.generic.GenericMapper;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {UserRoleMapper.class, SubscriptionPlanMapper.class})
@RequiredArgsConstructor
public abstract class UserMapper implements GenericMapper<User, UserDTO> {

    @Autowired
    protected S3Service s3Service;

    @Mapping(target = "image", source = "imageKey", qualifiedByName = "mapImageKeyToImage")
    public abstract UserDTO toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isAdmin", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "imageKey", ignore = true)
    @Mapping(target = "stripeSubscriptionId", ignore = true)
    public abstract User toEntity(UserDTO userDTO);

    @Named("mapImageKeyToImage")
    protected String mapMediaToImage(String imageKey) {
        if (imageKey == null) {
            return null;
        }
        return s3Service.generatePresignedUrl(imageKey);
    }

}