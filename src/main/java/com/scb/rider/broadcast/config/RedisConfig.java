package com.scb.rider.broadcast.config;

import io.lettuce.core.ReadFrom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.util.Collections;
import java.util.List;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@Slf4j
@Profile("!test")
@EnableRedisRepositories
public class RedisConfig {

    private static final int DEFAULT_PORT = 6379;

    @Value("${redis.primaryNode}")
    private String redisHostPort;

    @Value("${redis.readNode}")
    private String readNode;

    @Bean
    LettuceConnectionFactory jedisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();
        RedisStaticMasterReplicaConfiguration clusterConfiguration = new RedisStaticMasterReplicaConfiguration(redisHostPort);
        clusterConfiguration.addNode(readNode, DEFAULT_PORT);
        return new LettuceConnectionFactory(clusterConfiguration, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.json());
        template.setValueSerializer(RedisSerializer.java());
        return template;
    }
}
