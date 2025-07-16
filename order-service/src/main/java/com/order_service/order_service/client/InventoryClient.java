package com.order_service.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service", url = "${inventory.service.url}")
public interface InventoryClient {
    
    @GetMapping("/api/inventory/check/{productId}")
    public Integer checkStock(@PathVariable("productId") String productId);
}
