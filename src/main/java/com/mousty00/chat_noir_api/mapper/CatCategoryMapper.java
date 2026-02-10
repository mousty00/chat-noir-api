package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.dto.CatCategoryDTO;
import com.mousty00.chat_noir_api.entity.CatCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CatCategoryMapper extends BaseMapper<CatCategory, CatCategoryDTO> {
}