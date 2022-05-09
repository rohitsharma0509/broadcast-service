package com.scb.rider.broadcast.service;

import com.scb.rider.broadcast.constants.BroadcastServiceConstants;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private HashOperations<String, Object, Object> hashOperations;

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    public void put(String key, Object hashKey, Object value) {
        put(key, hashKey, value, null);
    }

    public void put(String key, Object hashKey, Object value, Long keyExpiryTime) {
        keyExpiryTime = Objects.nonNull(keyExpiryTime) ? keyExpiryTime : BroadcastServiceConstants.DEFAULT_REDIS_CACHE_TTL;
        log.info("updating cache for key {}, keyExpiryTime {}", key, keyExpiryTime);
        redisTemplate.expire(key, keyExpiryTime, TimeUnit.SECONDS);
        hashOperations.put(key, hashKey, value);
    }

    public List<Object> values(String key) {
        return hashOperations.values(key);
    }

    public Object get(String key, Object hashKey) {
        return hashOperations.get(key, hashKey);
    }

    public Long size(String key) {
        return hashOperations.size(key);
    }

    public Boolean deleteAll(String key) {
        return redisTemplate.delete(key);
    }

    public Long delete(String key, Object hashKey) {
        return delete(key, new Object[] { hashKey });
    }

    public Long delete(String key, Object[] hashKeys) {
        return hashOperations.delete(key, hashKeys);
    }
}
