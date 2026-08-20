package com.rahul.realtime.notification.controller;

import com.rahul.realtime.notification.sse.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationStreamController {

    private final SseConnectionManager sseConnectionManager;

    @GetMapping(value = "/stream/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@PathVariable String userId) {

        SseEmitter emitter = sseConnectionManager.addEmitter(userId);

        try {

            emitter.send(SseEmitter.event().name("connected").reconnectTime(3000).data("SSE connection established"));

        } catch (Exception exception) {

            emitter.completeWithError(exception);
        }

        return emitter;
    }
}