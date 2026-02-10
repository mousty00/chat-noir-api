package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.dto.CatDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CatCategoryMapper.class})
public interface CatMapper extends BaseMapper<Cat, CatDTO> {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "image", target = "media.contentUrl")
    Cat toEntity(CatDTO dto);

    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "media.contentUrl", target = "image")
    CatDTO toDTO(Cat entity);

}