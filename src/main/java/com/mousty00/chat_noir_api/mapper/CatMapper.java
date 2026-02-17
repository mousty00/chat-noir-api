package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.aws.S3Service;
import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatCategory;
import com.mousty00.chat_noir_api.entity.CatMedia;
import com.mousty00.chat_noir_api.generic.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = {CatCategoryMapper.class})
public abstract class CatMapper implements GenericMapper<Cat, CatDTO> {

    @Autowired
    protected S3Service s3Service;

    @Mapping(target = "media", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract Cat toEntity(CatDTO dto);

    @Mapping(target = "image", source = "media", qualifiedByName = "mapMediaToImage")
    public abstract CatDTO toDTO(Cat entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "media", ignore = true)
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "idToCategory")
    public abstract Cat toEntityFromRequest(CatRequestDTO request);

    @Named("mapMediaToImage")
    protected String mapMediaToImage(CatMedia media) {
        if (media == null || media.getMediaKey() == null) {
            return null;
        }
        return s3Service.generatePresignedUrl(media.getMediaKey());
    }

    @Named("idToCategory")
    protected CatCategory idToCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        CatCategory category = new CatCategory();
        category.setId(categoryId);
        return category;
    }
}