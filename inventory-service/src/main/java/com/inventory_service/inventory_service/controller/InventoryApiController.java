package com.inventory_service.inventory_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory_service.inventory_service.model.dto.InventoryDTO;
import com.inventory_service.inventory_service.model.request.InventoryRequest;
import com.inventory_service.inventory_service.service.InventoryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
public class InventoryApiController {

    @Autowired private InventoryService inventoryService;

    @GetMapping("/check/{productId}")
    public ResponseEntity<Integer> checkStock(@PathVariable("productId") String productId) {
        InventoryDTO response = inventoryService.findById(productId);
        if (response != null) {
            log.info("(Feign) (api) (check-stock) {} stock: {}", productId, response.getStock());
            return ResponseEntity.ok(response.getStock());
        }
        log.info("(Feign) (api) (check-stock) {} stock: {}", productId, 0);
        return ResponseEntity.ok(0);
    }

    @PostMapping("/decrement")
    public ResponseEntity<Integer> decrementStock(@RequestBody InventoryRequest request) {
        Integer stock = inventoryService.decrementStock(request.getProductId(), request.getStock());
        if (stock != null) {
            log.info("(Feign) (api) (update-stock) {} stock: {}", request.getProductId(), stock - request.getStock());
            return ResponseEntity.ok(stock - request.getStock());
        }
        log.info("(Feign) (api) (update-stock) {} stock: {}", request.getProductId(), 0);
        return ResponseEntity.ok(0);
    }
}
