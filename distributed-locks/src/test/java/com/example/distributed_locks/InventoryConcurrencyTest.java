package com.example.distributed_locks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class InventoryConcurrencyTest {

    @Test
    void testConcurentPurchases() throws Exception {
        int numberOfRequests = 50;

        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(numberOfRequests);

        RestTemplate restTemplate = new RestTemplate();

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    restTemplate.postForObject("http://localhost:8080/inventory/purchase/1", null, String.class);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
    }
}
