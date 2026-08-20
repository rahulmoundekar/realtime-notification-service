package com.rahul.realtime.notification.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketConnectionManager connectionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        String userId = extractUserId(session);

        connectionManager.addSession(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {

        log.info("WebSocket message received. sessionId={}, payload={}", session.getId(), message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {

        log.warn("WebSocket transport error. sessionId={}", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {

        String userId = extractUserId(session);

        connectionManager.removeSession(userId, session);
    }

    private String extractUserId(WebSocketSession session) {

        if (session.getUri() == null) {
            throw new IllegalStateException("WebSocket session URI is not available");
        }

        String path = session.getUri().getPath();

        String[] parts = path.split("/");

        return parts[parts.length - 1];
    }
}