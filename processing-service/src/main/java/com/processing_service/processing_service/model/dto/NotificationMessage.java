package com.processing_service.processing_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationMessage {
    private String type;
    private Object data;

    public static NotificationMessage success(String orderJson) {
        return new NotificationMessage("PROCESSING_SERVICE_OK", orderJson);
    }

    public static NotificationMessage error(String message) {
        return new NotificationMessage("PROCESSING_SERVICE_ERROR", message);
    }

    public static NotificationMessage unknown(String raw) {
        return new NotificationMessage("PROCESSING_SERVICE_UNKNOWN_ERROR", raw);
    }
}
