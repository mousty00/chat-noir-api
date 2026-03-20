package com.mousty00.chat_noir_api.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource railwayDataSource(
            @Value("${DATABASE_URL}") String databaseUrl,
            @Value("${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE:5}") int maxPoolSize,
            @Value("${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE:1}") int minIdle) {

        URI uri = URI.create(databaseUrl.replace("postgresql://", "http://"));
        String[] userInfo = uri.getUserInfo().split(":", 2);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql://%s:%d%s", uri.getHost(), uri.getPort(), uri.getPath()));
        config.setUsername(userInfo[0]);
        config.setPassword(userInfo.length > 1 ? userInfo[1] : "");
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(20000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
