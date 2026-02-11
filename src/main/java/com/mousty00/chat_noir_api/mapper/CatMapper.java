package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = {CatCategoryMapper.class})
public interface CatMapper extends BaseMapper<Cat, CatDTO> {

    @Mapping(target = "media", ignore = true)
    @Mapping(target = "id", ignore = true)
    Cat toEntity(CatDTO dto);

    @Mapping(source = "media.contentUrl", target = "image")
    CatDTO toDTO(Cat entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "media", ignore = true)
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "idToCategory")
    Cat toEntityFromRequest(CatRequestDTO request);

    @Named("idToCategory")
    default CatCategory idToCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        CatCategory category = new CatCategory();
        category.setId(categoryId);
        return category;
    }
}