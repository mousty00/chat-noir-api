package com.mousty00.chat_noir_api.mapper;

public interface BaseMapper <ENTITY, DTO>{

    DTO toDTO(ENTITY entity);

    ENTITY toEntity(DTO dto);
}
