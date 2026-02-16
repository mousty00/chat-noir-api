package com.mousty00.chat_noir_api.mapper;

import com.mousty00.chat_noir_api.dto.user.SubscriptionPlanDTO;
import com.mousty00.chat_noir_api.entity.SubscriptionPlan;
import com.mousty00.chat_noir_api.generic.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionPlanMapper extends GenericMapper<SubscriptionPlan, SubscriptionPlanDTO> {
    
    @Override
    SubscriptionPlanDTO toDTO(SubscriptionPlan subscriptionPlan);

    @Override
    @Mapping(target = "id", ignore = true)
    SubscriptionPlan toEntity(SubscriptionPlanDTO subscriptionPlanDTO);
}