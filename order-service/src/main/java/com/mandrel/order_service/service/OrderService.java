package com.mandrel.order_service.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mandrel.order_service.client.InventoryClient;
import com.mandrel.order_service.messaging.OrderEventPublisher;
import com.mandrel.order_service.model.dto.OrderDTO;
import com.mandrel.order_service.model.dto.OrderItemDTO;
import com.mandrel.order_service.model.entity.Order;
import com.mandrel.order_service.model.entity.OrderItem;
import com.mandrel.order_service.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private InventoryClient inventoryClient;
    @Autowired private OrderEventPublisher orderEventPublisher;

    public OrderDTO createOrder(Order order) {
        OrderDTO orderDTO = this.buildOrderWithItemsStock(order);
        order.setStatus("CREATED");
        order.setOrderDate(Timestamp.from(Instant.now()));

        if (!orderDTO.getItemsOutOfStockMessage().isEmpty()) {
            order.setStatus("FAILED");
        }

        Order newOrder = orderRepository.save(order);
        this.synchOrderItems(orderDTO, newOrder);

        orderEventPublisher.publishOrderToProcessEvent(newOrder.toString());
        orderEventPublisher.publishNotificationEvent(newOrder.toString());
        return orderDTO;
    }

    private void synchOrderItems(OrderDTO orderDTO, Order order) {
        orderDTO.setId(order.getId());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setOrderDate(order.getOrderDate());

        Map<String, OrderItem> currentOrderItems = order.getItems().stream()
            .collect(Collectors.toMap(OrderItem::getProductId, Function.identity()));

        for (OrderItemDTO orderItem : orderDTO.getItems()) {
            if (currentOrderItems.containsKey(orderItem.getProductId())) {
                orderItem.setId(currentOrderItems.get(orderItem.getProductId()).getId());
            }
        }
    }

    public List<OrderDTO> findAll() {
        List<OrderDTO> orders = new ArrayList<>();
        for (Order order : orderRepository.findAll()) {
            orders.add(this.buildOrderWithItemsStock(order));
        }
        return orders;
    }

    public OrderDTO findById(Long id) {
        Optional<Order> response = orderRepository.findById(id);
        if (response.isPresent()) {
            return this.buildOrderWithItemsStock(response.get());
        }
        return null;
    }

    private OrderDTO buildOrderWithItemsStock(Order order) {
        List<OrderItemDTO> orderItems = new ArrayList<>();
        StringBuilder errorMessage = new StringBuilder("");
        for (OrderItem orderItem : order.getItems()) {
            Integer stock = inventoryClient.checkStock(orderItem.getProductId());
            if (stock < orderItem.getQuantity()) {
                if (errorMessage.isEmpty()) {
                    errorMessage.append("The following items are out of stock:");
                }
                errorMessage.append("\n • ")
                    .append(orderItem.getProductId())
                    .append(" (requested ").append(orderItem.getQuantity()).append(")")
                    .append(" (stock ").append(stock).append(")");
            }
            orderItems.add(OrderItemDTO.Builder.build(orderItem, stock));
        }
        return OrderDTO.Builder.build(order, orderItems, errorMessage.toString());
    }

    public OrderDTO deleteById(Long id) {
        Optional<Order> response = orderRepository.findById(id);
        if (response.isPresent()) {
            orderRepository.deleteById(id);
            response.get().setStatus("DELETED");
            return this.buildOrderWithItemsStock(response.get());
        }
        return null;
    }

    public OrderDTO updateOrderById(Long id, Order changes) {
        Optional<Order> response = orderRepository.findById(id);
        if (response.isPresent()) {
            Order order = response.get();
            order.setOrderDate(Timestamp.from(Instant.now()));
            this.updateOrderItem(order, changes);
            this.removeOrderItem(order, changes);

            OrderDTO orderDTO = this.buildOrderWithItemsStock(response.get());
            if (!orderDTO.getItemsOutOfStockMessage().isEmpty()) {
                order.setStatus("FAILED");
            } else {
                order.setStatus("UPDATED");
            }

            Order newOrder = orderRepository.save(order);
            this.synchOrderItems(orderDTO, newOrder); 

            orderEventPublisher.publishOrderToProcessEvent(newOrder.toString());
            orderEventPublisher.publishNotificationEvent(newOrder.toString());
            return orderDTO;
        }
        return null;
    }

    private void updateOrderItem(Order order, Order changes) {
        Map<String, OrderItem> currentOrderItems = order.getItems().stream()
            .collect(Collectors.toMap(OrderItem::getProductId, Function.identity()));

        for (OrderItem newOrderItem : changes.getItems()) {
            String productId = newOrderItem.getProductId();
        
            if (currentOrderItems.containsKey(productId)) {
                currentOrderItems.get(productId).setQuantity(newOrderItem.getQuantity());
            } else {
                newOrderItem.setOrder(order);
                order.getItems().add(newOrderItem);
            }
        }
    }

    private void removeOrderItem(Order order, Order changes) {
        Map<String, OrderItem> newOrderItems = changes.getItems().stream()
            .collect(Collectors.toMap(OrderItem::getProductId, Function.identity()));

        Iterator<OrderItem> iterator = order.getItems().iterator();
        while (iterator.hasNext()) {
            if (!newOrderItems.containsKey(iterator.next().getProductId())) {
                iterator.remove();
            }
        }
    }

    public OrderDTO closeOrder(Long id) {
        Optional<Order> response = orderRepository.findById(id);
        if (response.isPresent()) {
            Order order = response.get();
            order.setOrderDate(Timestamp.from(Instant.now()));
            order.setStatus("CLOSED");
            return OrderDTO.Builder.build(orderRepository.save(order));
        }
        return null;
    }
}
