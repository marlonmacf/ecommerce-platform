package com.processing_service.processing_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEvent {

    private Long id;
    private String productId;
    private Integer quantity;

    @Override
    public String toString() {
        return "{id:" + id + ", productId:\"" + productId + "\", quantity:" + quantity + "}";
    }
}
