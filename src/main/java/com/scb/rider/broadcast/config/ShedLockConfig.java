package com.scb.rider.broadcast.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.MongoLockProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(value = "!test")
public class ShedLockConfig {

    @Value("${mongo.dbName}")
    private String dbName;

    @Autowired
    private MongoConfig mongoConfig;

    @Bean
    public LockProvider lockProvider() {
        return new MongoLockProvider(mongoConfig.mongoClient().getDatabase(dbName));
    }
}
