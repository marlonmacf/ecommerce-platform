package com.notification_service.notification_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.notification_service.notification_service.service.SseEmitterService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    
    @Autowired private SseEmitterService sseService;

    @GetMapping("/stream")
    public SseEmitter streamNotifications() {
        return sseService.createEmitter();
    }
}
