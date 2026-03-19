package com.mousty00.chat_noir_api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        // Paginated cat list
        manager.registerCustomCache("cats",
                Caffeine.newBuilder()
                        .maximumSize(200)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .recordStats()
                        .build());

        // Single cat by ID
        manager.registerCustomCache("cat",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .recordStats()
                        .build());

        // Categories rarely change
        manager.registerCustomCache("categories",
                Caffeine.newBuilder()
                        .maximumSize(50)
                        .expireAfterWrite(60, TimeUnit.MINUTES)
                        .recordStats()
                        .build());

        return manager;
    }
}
