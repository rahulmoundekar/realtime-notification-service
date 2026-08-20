package com.rahul.realtime.notification.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketCleanupScheduler {

    private final WebSocketConnectionManager connectionManager;

    @Scheduled(fixedRate = 60_000)
    public void cleanup() {

        connectionManager.cleanupClosedSessions();
    }
}