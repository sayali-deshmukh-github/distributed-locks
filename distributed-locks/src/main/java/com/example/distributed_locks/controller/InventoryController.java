package com.example.distributed_locks.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.distributed_locks.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/purchase/{id}")
    public ResponseEntity<String> purchase(@PathVariable Long id) {
        boolean success = inventoryService.purchase(id);

        return success 
            ? ResponseEntity.ok("Purchase successful") 
            : ResponseEntity.badRequest().body("Out of stock");

    }

}
