package com.rahul.realtime.notification.service.impl;

import com.rahul.realtime.notification.dto.event.NotificationEvent;
import com.rahul.realtime.notification.service.NotificationDeliveryDeduplicator;
import com.rahul.realtime.notification.service.NotificationDeliveryService;
import com.rahul.realtime.notification.sse.SseConnectionManager;
import com.rahul.realtime.notification.websocket.WebSocketConnectionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryServiceImpl implements NotificationDeliveryService {

    private final WebSocketConnectionManager connectionManager;

    private final SseConnectionManager sseConnectionManager;

    private final ObjectMapper objectMapper;

    private final NotificationDeliveryDeduplicator
            deduplicator;

    @Override
    public void deliver(NotificationEvent event) {

        if (!deduplicator.shouldDeliver(
                event.notificationId()
        )) {

            log.info(
                    "Duplicate notification skipped. " +
                            "notificationId={}, userId={}",
                    event.notificationId(),
                    event.userId()
            );

            return;
        }

        deliverViaWebSocket(event);

        deliverViaSse(event);
    }

    private void deliverViaWebSocket(NotificationEvent event) {

        try {

            String payload = objectMapper.writeValueAsString(event);

            for (WebSocketSession session : connectionManager.getSessions(event.userId())) {

                if (!session.isOpen()) {
                    continue;
                }

                session.sendMessage(new TextMessage(payload));

                log.info("Notification delivered via WebSocket. notificationId={}, userId={}, sessionId={}", event.notificationId(), event.userId(), session.getId());
            }

        } catch (JacksonException exception) {

            log.error("Failed to serialize WebSocket notification. notificationId={}", event.notificationId(), exception);

        } catch (IOException exception) {

            log.error("Failed to send WebSocket notification. notificationId={}, userId={}", event.notificationId(), event.userId(), exception);
        }
    }


    private void deliverViaSse(NotificationEvent event) {

        sseConnectionManager.send(event.userId(), event);

        log.info("Notification delivered via SSE. notificationId={}, userId={}", event.notificationId(), event.userId());
    }
}