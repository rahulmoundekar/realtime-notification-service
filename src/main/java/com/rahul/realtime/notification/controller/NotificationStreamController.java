package com.rahul.realtime.notification.controller;

import com.rahul.realtime.notification.sse.SseConnectionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationStreamController {

    private final SseConnectionManager sseConnectionManager;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(Authentication authentication) {

        String userId = authentication.getName();

        SseEmitter emitter = sseConnectionManager.addEmitter(userId);

        try {

            emitter.send(SseEmitter.event().name("connected").reconnectTime(3000).data("SSE connection established"));

        } catch (IOException exception) {

            emitter.completeWithError(exception);
        }

        return emitter;
    }
}