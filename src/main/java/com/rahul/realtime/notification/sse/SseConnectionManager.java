package com.rahul.realtime.notification.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SseConnectionManager {

    private final ConcurrentHashMap<String, Set<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter addEmitter(String userId) {

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        userEmitters.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));

        emitter.onTimeout(() -> removeEmitter(userId, emitter));

        emitter.onError(throwable -> removeEmitter(userId, emitter));

        log.info("SSE connected. userId={}", userId);

        return emitter;
    }

    public void removeEmitter(String userId, SseEmitter emitter) {

        Set<SseEmitter> emitters = userEmitters.get(userId);

        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);

        if (emitters.isEmpty()) {
            userEmitters.remove(userId);
        }

        log.info("SSE disconnected. userId={}", userId);
    }

    public Set<SseEmitter> getEmitters(String userId) {

        return userEmitters.getOrDefault(userId, Collections.emptySet());
    }

    public void send(String userId, Object event) {

        Set<SseEmitter> emitters = userEmitters.get(userId);

        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {

            try {

                emitter.send(SseEmitter.event().name("notification").data(event));

            } catch (IOException exception) {

                removeEmitter(userId, emitter);

                log.warn("Failed to send SSE notification. userId={}", userId, exception);
            }
        }
    }
}