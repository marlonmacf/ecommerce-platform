package com.mandrel.order_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mandrel.order_service.model.dto.OrderDTO;
import com.mandrel.order_service.model.request.OrderEventRequest;
import com.mandrel.order_service.service.OrderService;

@RestController
@RequestMapping("/api/order")
public class OrderApiController {

    @Autowired private OrderService orderService;

    @PostMapping("/close")
    public ResponseEntity<Long> decrementStock(@RequestBody OrderEventRequest request) {
        OrderDTO order = orderService.closeOrder(request.getId());
        if (order != null) {
            return ResponseEntity.ok(order.getId());
        }
        return ResponseEntity.ok(Long.valueOf(0));
    }
}
