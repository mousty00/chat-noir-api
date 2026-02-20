package com.mousty00.chat_noir_api.util.generic;

public interface GenericMapper<ENTITY, DTO> {

    DTO toDTO(ENTITY entity);

    ENTITY toEntity(DTO dto);

}
