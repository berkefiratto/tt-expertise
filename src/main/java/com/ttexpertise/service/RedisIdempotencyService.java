package com.ttexpertise.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisIdempotencyService implements IdempotencyService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);
    
    @Autowired
    public RedisIdempotencyService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public boolean isDuplicate(String key) {
        return redisTemplate.hasKey(IDEMPOTENCY_KEY_PREFIX + key);
    }
    
    @Override
    public void markAsProcessed(String key, String result) {
        redisTemplate.opsForValue().set(
            IDEMPOTENCY_KEY_PREFIX + key, 
            result, 
            DEFAULT_TTL
        );
    }
    
    @Override
    public String getResult(String key) {
        return redisTemplate.opsForValue().get(IDEMPOTENCY_KEY_PREFIX + key);
    }
    
    @Override
    public void markAsProcessed(String key, String result, Duration ttl) {
        redisTemplate.opsForValue().set(
            IDEMPOTENCY_KEY_PREFIX + key, 
            result, 
            ttl
        );
    }
    
    @Override
    public void removeKey(String key) {
        redisTemplate.delete(IDEMPOTENCY_KEY_PREFIX + key);
    }
}
