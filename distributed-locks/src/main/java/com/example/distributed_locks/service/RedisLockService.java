package com.example.distributed_locks.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisLockService {
    private final StringRedisTemplate redisTemplate;

    public boolean acquireLock(String key, String value, long timeoutMillis) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMillis(timeoutMillis));
        if (Boolean.TRUE.equals(success)) {
            return true;
        }
        return false;
    }

    public boolean acquireLockWithRetry(String key, String value, long timeoutMillis, int maxRetries, long backoffMillis) {
        int attempt = 0;
        while (attempt < maxRetries) {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMillis(timeoutMillis));
            if (Boolean.TRUE.equals(success)) {
                return true;
            }
            attempt++;
            try {
                Thread.sleep(backoffMillis * attempt); // Exponential backoff
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false; // Failed to acquire lock after retries
    }

    public void releaseLock(String key, String value) {
        String currentValue = redisTemplate.opsForValue().get(key);
        if (value.equals(currentValue)) {
            redisTemplate.delete(key);
        }
    }
}
