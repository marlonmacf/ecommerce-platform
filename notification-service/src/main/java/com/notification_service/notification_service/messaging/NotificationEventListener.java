package com.notification_service.notification_service.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.notification_service.notification_service.model.NotificationMessage;
import com.notification_service.notification_service.service.SseEmitterService;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationEventListener {

    @Autowired private SimpMessagingTemplate webSocketMessenger;
    @Autowired private SseEmitterService sseEmitterService;

    private static final String WS_DESTINATION = "/notifications/stream";

    @SqsListener("notification-queue")
    public void handleNotificationEvent(String sqsMessage) {
         this.dispatchNotification(sqsMessage, "(notification-queue)");
    }

    public void dispatchNotification(String sqsMessage, String location) {
        try {
            NotificationMessage message = processMessage(sqsMessage, location);
            webSocketMessenger.convertAndSend(WS_DESTINATION, message);
            sseEmitterService.broadcast(message);
        } catch (Exception e) {
            NotificationMessage error = NotificationMessage.error(e.getMessage());
            webSocketMessenger.convertAndSend(WS_DESTINATION, error);
            sseEmitterService.broadcast(error);
        }
    }

    private NotificationMessage processMessage(String rawMessage, String location) {
        if (rawMessage.contains("id") && rawMessage.contains("items")) {
            log.info("(SQS) {} (consuming) Notifying: {}", location, rawMessage);
            return NotificationMessage.success(rawMessage);
        } else if (rawMessage.contains("out of stock")) {
            log.warn("(SQS) {} (consuming) Notifying: Stock error: {}", location, rawMessage);
            log.warn(location + " Consuming: Notifying: Stock error: {}", rawMessage);
            return NotificationMessage.error(rawMessage);
        } else {
            log.error("(SQS) {} (consuming) Notifying: Unknown error: {}", location, rawMessage);
            return NotificationMessage.unknown(rawMessage);
        }
    }
}