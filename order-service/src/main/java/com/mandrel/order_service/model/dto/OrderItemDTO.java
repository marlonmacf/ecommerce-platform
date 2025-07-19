package com.mandrel.order_service.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.mandrel.order_service.model.entity.OrderItem;

public class OrderItemDTO {

    private Long id;
    private String productId;
    private Integer quantity;
    private Integer stock;

    private OrderItemDTO(Builder builder) {
        this.id = builder.id;
        this.productId = builder.productId;
        this.quantity = builder.quantity;
        this.stock = builder.stock;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public static class Builder {
        private Long id;
        private String productId;
        private Integer quantity;
        private Integer stock;

        public Builder(Long id, String productId, Integer quantity, Integer stock) {
            this.id = id;
            this.productId = productId;
            this.quantity = quantity;
            this.stock = stock;
        }

        public static List<OrderItemDTO> build(List<OrderItem> orderItems) {
            List<OrderItemDTO> response = new ArrayList<>();
            for (OrderItem orderItem: orderItems) {
                response.add(build(orderItem));
            }
            return response;
        }

        public static OrderItemDTO build(OrderItem orderItem, Integer stock) {
            return new OrderItemDTO(new Builder(orderItem.getId(), orderItem.getProductId(), orderItem.getQuantity(), stock));
        }

        public static OrderItemDTO build(OrderItem orderItem) {
            return new OrderItemDTO(new Builder(orderItem.getId(), orderItem.getProductId(), orderItem.getQuantity(), null));
        }

        public OrderItemDTO build() {
            return new OrderItemDTO(this);
        }
    }
}
