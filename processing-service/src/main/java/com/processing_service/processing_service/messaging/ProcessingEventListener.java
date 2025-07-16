package com.processing_service.processing_service.messaging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.processing_service.processing_service.client.InventoryClient;
import com.processing_service.processing_service.client.OrderClient;
import com.processing_service.processing_service.model.dto.NotificationMessage;
import com.processing_service.processing_service.model.dto.OrderEvent;
import com.processing_service.processing_service.model.dto.OrderItemEvent;
import com.processing_service.processing_service.model.request.InventoryRequest;
import com.processing_service.processing_service.model.request.OrderRequest;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProcessingEventListener {

    @Autowired private ProcessingEventPublisher processingEventPublisher;
    @Autowired private InventoryClient inventoryClient;
    @Autowired private OrderClient orderClient;

    @SqsListener("processing-queue")
    public void handleOrderEvent(String sqsMessage) {
         this.dispatchNotification(sqsMessage);
    }

    public void dispatchNotification(String sqsMessage) {
        NotificationMessage message = processMessage(sqsMessage);
        processingEventPublisher.publishNotificationEvent(message.toString());
    }

    private NotificationMessage processMessage(String rawMessage) {
        if (rawMessage.contains("id") && rawMessage.contains("items")) {
            OrderEvent orderEvent = deserialize(rawMessage);
            List<String> response = new ArrayList<>();
            for (OrderItemEvent orderItemEvent : orderEvent.getItems()) {
                Integer updatedStock = inventoryClient.decrementStock(new InventoryRequest(orderItemEvent.getProductId(), orderItemEvent.getQuantity()));
                String message = "{\"productId\": " + orderItemEvent.getProductId() + " \"stock\": " + updatedStock + "}";
                response.add(message);
            }
            orderClient.closeOrder(new OrderRequest(orderEvent.getId()));
            log.info("(SQS) (order-queue) (consuming) Closing order: {} items: {}", rawMessage, response);
            return NotificationMessage.success(rawMessage);
        } else if (rawMessage.contains("out of stock")) {
            log.warn("(SQS) (order-queue) (consuming) Closing order: Items out of stock error: {}", rawMessage);
            return NotificationMessage.error(rawMessage);
        } else {
            log.error("(SQS) (order-queue) (consuming) Closing order: Unknown message type: {}" + rawMessage);
            return NotificationMessage.unknown(rawMessage);
        }
    }

    private OrderEvent deserialize(String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(body, OrderEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON in SQS body", e);
            throw new RuntimeException(e);
        }
    }
}