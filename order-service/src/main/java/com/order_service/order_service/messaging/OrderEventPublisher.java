package com.order_service.order_service.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderEventPublisher {

    @Autowired private SqsTemplate sqsTemplate;
    private static final String PROCESSING_QUEUE_NAME = "processing-queue";
    private static final String NOTIFICATION_QUEUE_NAME = "notification-queue";

    public String publishOrderToProcessEvent(String message) {
        return this.publishEvent(PROCESSING_QUEUE_NAME, message);
    }

    public String publishNotificationEvent(String message) {
        return this.publishEvent(NOTIFICATION_QUEUE_NAME, message);
    }

    private String publishEvent(String queueName, String message) {
        try {
            var sendResult = sqsTemplate.send(queueName, message);
            log.info("(SQS) ({}) (publishing) {}", queueName, message);
            return sendResult.messageId().toString();
        } catch (Exception e) {
            throw new RuntimeException("(SQS) (" + queueName + ") (publishing) " + message + ": Failed to publish event to queue\n", e);
        }
    }
}
