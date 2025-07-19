package com.mandrel.processing_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mandrel.processing_service.model.request.OrderRequest;

@FeignClient(name = "order-service", url = "${order.service.url}")
public interface OrderClient {
    
    @PostMapping("/api/order/close")
    public Integer closeOrder(@RequestBody OrderRequest request);
}