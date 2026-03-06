package com.example.distributed_locks.service;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedissonLockService {

    private final RedissonClient redissonClient;

    // acquire lock with retries and backoff
    public boolean executeWithLock(String key, Runnable task, int maxRetries, long backoffMillis, long leaseTimeMillis) {
        RLock lock = redissonClient.getLock(key);
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                if (lock.tryLock(0, leaseTimeMillis, TimeUnit.MILLISECONDS)) {
                    try {
                        task.run();
                        return true; // Task executed successfully
                    } finally {
                        lock.unlock();
                    }
                } else {
                    attempt++;
                    log.warn("Lock busy for key={} (attempt {})", key, attempt);
                    Thread.sleep(backoffMillis * attempt); // Exponential backoff
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Lock acquisition interrupted while waiting for lock on key={}", key, e);
                return false;
            }
        }
        log.error("Failed to acquire lock for key={} after {} attempts", key, maxRetries);
        return false; // Failed to acquire lock after retries
    }

}
