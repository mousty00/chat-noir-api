package com.mousty00.chat_noir_api.repository;

import com.mousty00.chat_noir_api.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
}
