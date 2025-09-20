package com.ttexpertise.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "idempotency.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryIdempotencyService implements IdempotencyService {
    
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    
    @Override
    public boolean isDuplicate(String key) {
        return cache.containsKey(key);
    }
    
    @Override
    public void markAsProcessed(String key, String result) {
        cache.put(key, result);
        // Simple cleanup after 1 hour
        new Thread(() -> {
            try {
                TimeUnit.HOURS.sleep(1);
                cache.remove(key);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    @Override
    public String getResult(String key) {
        return cache.get(key);
    }
    
    @Override
    public void removeKey(String key) {
        cache.remove(key);
    }
}
