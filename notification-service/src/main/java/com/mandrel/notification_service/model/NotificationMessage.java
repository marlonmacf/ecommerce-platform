package com.mandrel.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationMessage {
    private String type;
    private Object data;

    public static NotificationMessage success(String orderJson) {
        return new NotificationMessage("NOTIFICATION_SERVICE_OK", orderJson);
    }

    public static NotificationMessage error(String error) {
        return new NotificationMessage("NOTIFICATION_SERVICE_ERROR", error);
    }

    public static NotificationMessage unknown(String raw) {
        return new NotificationMessage("NOTIFICATION_SERVICE_UNKNOWN_ERROR", raw);
    }
}
