package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CatCategoryMapper.class})
public interface CatMapper extends BaseMapper<Cat, CatDTO> {

    @Mapping(target = "media", ignore = true)
    @Mapping(target = "id", ignore = true)
    Cat toEntity(CatDTO dto);

    @Mapping(source = "media.contentUrl", target = "image")
    CatDTO toDTO(Cat entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "media", ignore = true)
    Cat toEntityFromRequest(CatRequestDTO request);
}