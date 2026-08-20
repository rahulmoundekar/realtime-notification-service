package com.rahul.realtime.notification.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHeartbeatService {

    private final WebSocketConnectionManager connectionManager;

    @Scheduled(fixedRate = 30_000)
    public void sendHeartbeat() {

        connectionManager.getAllSessions().forEach(session -> {

            if (!session.isOpen()) {
                return;
            }

            try {

                session.sendMessage(new PingMessage());

                log.debug("WebSocket heartbeat sent. sessionId={}", session.getId());

            } catch (Exception exception) {

                log.warn("WebSocket heartbeat failed. sessionId={}", session.getId(), exception);
            }
        });
    }
}