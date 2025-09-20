package com.ttexpertise.service;

import java.time.Duration;

public interface IdempotencyService {
    
    boolean isDuplicate(String key);
    
    void markAsProcessed(String key, String result);
    
    String getResult(String key);
    
    default void markAsProcessed(String key, String result, Duration ttl) {
        markAsProcessed(key, result);
    }
    
    default void removeKey(String key) {
        // Default implementation does nothing
    }
}
