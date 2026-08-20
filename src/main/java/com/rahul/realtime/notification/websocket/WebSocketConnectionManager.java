package com.rahul.realtime.notification.websocket;

import com.rahul.realtime.notification.redis.RedisConnectionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketConnectionManager {

    private final RedisConnectionRegistry connectionRegistry;

    private final ConcurrentHashMap<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void addSession(String userId, WebSocketSession session) {

        userSessions.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);

        connectionRegistry.register(userId);

        log.info("WebSocket connected. userId={}, sessionId={}", userId, session.getId());
    }

    public void removeSession(String userId, WebSocketSession session) {

        Set<WebSocketSession> sessions = userSessions.get(userId);

        if (sessions == null) {
            return;
        }

        sessions.remove(session);

        if (sessions.isEmpty()) {

            userSessions.remove(userId);

            connectionRegistry.unregister(userId);
        }

        log.info("WebSocket disconnected. userId={}, sessionId={}", userId, session.getId());
    }

    public Set<WebSocketSession> getSessions(String userId) {

        return userSessions.getOrDefault(userId, Collections.emptySet());
    }
}