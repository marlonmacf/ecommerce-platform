package com.mandrel.processing_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mandrel.processing_service.model.request.InventoryRequest;

@FeignClient(name = "inventory-service", url = "${inventory.service.url}")
public interface InventoryClient {

    @PostMapping("/api/inventory/decrement")
    public Integer decrementStock(@RequestBody InventoryRequest request);
}