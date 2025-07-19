package com.mandrel.order_service.model.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.mandrel.order_service.model.entity.Order;

public class OrderDTO {

    private Long id;
    private String status;
    private Timestamp orderDate;
    private List<OrderItemDTO> items;
    private String ItemsOutOfStockMessage;

    private OrderDTO(Builder builder) {
        this.id = builder.id;
        this.status = builder.status;
        this.orderDate = builder.orderDate;
        this.items = builder.items;
        this.ItemsOutOfStockMessage = builder.ItemsOutOfStockMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    public String getItemsOutOfStockMessage() {
        return ItemsOutOfStockMessage;
    }

    public void setItemsOutOfStockMessage(String itemsOutOfStockMessage) {
        ItemsOutOfStockMessage = itemsOutOfStockMessage;
    }

    public static class Builder {
        private Long id;
        private String status;
        private Timestamp orderDate;
        private List<OrderItemDTO> items;
        private String ItemsOutOfStockMessage;

        public Builder(Long id, String status, Timestamp orderDate, List<OrderItemDTO> items, String ItemsOutOfStockMessage) {
            this.id = id;
            this.status = status;
            this.orderDate = orderDate;
            this.items = items;
            this.ItemsOutOfStockMessage = ItemsOutOfStockMessage;
        }

        public static List<OrderDTO> build(List<Order> orders) {
            List<OrderDTO> response = new ArrayList<>();
            for (Order order: orders) {
                response.add(build(order));
            }
            return response;
        }

        public static OrderDTO build(Order order, List<OrderItemDTO> itemsOutOfStock, String ItemsOutOfStockMessage) {
            return new OrderDTO(new Builder(order.getId(), order.getStatus(), order.getOrderDate(), itemsOutOfStock, ItemsOutOfStockMessage));
        }

        public static OrderDTO build(Order order) {
            return new OrderDTO(new Builder(order.getId(), order.getStatus(), order.getOrderDate(), OrderItemDTO.Builder.build(order.getItems()), ""));
        }

        public OrderDTO build() {
            return new OrderDTO(this);
        }
    }
}
