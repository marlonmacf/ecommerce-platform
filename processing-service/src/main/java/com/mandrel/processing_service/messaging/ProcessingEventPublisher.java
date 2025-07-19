package com.mandrel.processing_service.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProcessingEventPublisher {

    @Autowired private SqsTemplate sqsTemplate;
    private static final String NOTIFICATION_QUEUE_NAME = "notification-queue";

    public String publishNotificationEvent(String message) {
        return this.publishEvent(NOTIFICATION_QUEUE_NAME, message);
    }

    private String publishEvent(String queueName, String message) {
        try {
            log.info("(SQS) ({}) (publishing) {}", queueName, message);
            var sendResult = sqsTemplate.send(queueName, message);
            return sendResult.messageId().toString();
        } catch (Exception e) {
            throw new RuntimeException("(SQS) (" + queueName + ") (publishing) " + message + ": Failed to publish event to queue\n", e);
        }
    }
}
