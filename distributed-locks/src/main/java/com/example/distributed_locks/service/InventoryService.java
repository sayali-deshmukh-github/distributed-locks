package com.example.distributed_locks.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.distributed_locks.entity.Inventory;
import com.example.distributed_locks.repository.InventoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final RedisLockService redisLockService;
    private final RedissonLockService redissonLockService;

    @Transactional
    public boolean purchase(Long productId) {
        String lockKey = "lock:product:" + productId;

        return redissonLockService.executeWithLock(
            lockKey, () -> {
                Inventory inventory = inventoryRepository.findById(productId).orElseThrow();
                if (inventory.getQuantity() <= 0) {
                    throw new RuntimeException("Out of stock");
                }
                inventory.setQuantity(inventory.getQuantity() - 1);
                inventoryRepository.save(inventory);
            },
            5, // max retries
            100, // backoff millis
            5000 // lease time millis
        );
    }

}
