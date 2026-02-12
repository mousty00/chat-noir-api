package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.entity.CatCategory;
import com.mousty00.chat_noir_api.generic.GenericMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CatCategoryMapper extends GenericMapper<CatCategory, CatCategoryDTO> {
}