package com.notification_service.notification_service.service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.notification_service.notification_service.model.NotificationMessage;

@Service
public class SseEmitterService {

    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(60_000L);
        emitters.add(emitter);
        
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        
        startHeartbeat(emitter);
        return emitter;
    }

    public void broadcast(NotificationMessage message) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                    .data(message)
                    .id(UUID.randomUUID().toString()));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        });
    }

    private void startHeartbeat(SseEmitter emitter) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (Exception e) {
                executor.shutdown();
            }
        }, 0, 25, TimeUnit.SECONDS);
        
        emitter.onCompletion(executor::shutdown);
    }
}
