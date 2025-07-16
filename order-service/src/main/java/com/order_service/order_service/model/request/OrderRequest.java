package com.order_service.order_service.model.request;

import java.util.ArrayList;
import java.util.List;

import com.order_service.order_service.model.entity.Order;
import com.order_service.order_service.model.entity.OrderItem;

public class OrderRequest {

    private List<OrderItemRequest> orderItems;

    public List<OrderItemRequest> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemRequest> orderItems) {
        this.orderItems = orderItems;
    }

    public Order toOrder() {
        
        Order order = new Order();
        List<OrderItem> orderItems = new ArrayList<>();
        
        if (this.orderItems != null) {
            for (OrderItemRequest orderItem: this.orderItems) {
                orderItems.add(orderItem.toOrderItem(order));
            }
        }
        
        order.setItems(orderItems);
        
        return order;
    }
}
