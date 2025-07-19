package com.mandrel.order_service.model.request;

import com.mandrel.order_service.model.entity.Order;
import com.mandrel.order_service.model.entity.OrderItem;

public class OrderItemRequest {

    private String productId;
    private Integer quantity;

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

    public OrderItem toOrderItem(Order order) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(productId);
        orderItem.setQuantity(quantity);
        orderItem.setOrder(order);
        return orderItem;
    }
}
