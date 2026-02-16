package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.dto.user.UserDTO;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.entity.UserMedia;
import com.mousty00.chat_noir_api.generic.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {UserRoleMapper.class, SubscriptionPlanMapper.class})
public interface UserMapper extends GenericMapper<User, UserDTO> {

    @Override
    @Mapping(target = "image", source = "profileMedia", qualifiedByName = "mapMediaToImage")
    UserDTO toDTO(User user);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isAdmin", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "profileMedia", source = "image", qualifiedByName = "mapImageToMedia")
    @Mapping(target = "stripeSubscriptionId", ignore = true)
    User toEntity(UserDTO userDTO);

    @Named("mapMediaToImage")
    default String mapMediaToImage(UserMedia profileMedia) {
        return profileMedia != null ? profileMedia.getUrl() : null;
    }

    @Named("mapImageToMedia")
    default UserMedia mapImageToMedia(String image) {
        if (image == null) {
            return null;
        }
        // This creates a partial UserMedia object.
        // the image is fetched on the service layer
        return UserMedia.builder()
                .url(image)
                .build();
    }
}