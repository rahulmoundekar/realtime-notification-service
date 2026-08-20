package com.rahul.realtime.notification.redis;

import com.rahul.realtime.notification.dto.event.NotificationEvent;
import com.rahul.realtime.notification.service.NotificationDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationSubscriber
        implements MessageListener {

    private final ObjectMapper objectMapper;

    private final NotificationDeliveryService
            notificationDeliveryService;

    @Value("${app.redis.notification-channel}")
    private String notificationChannel;

    @Override
    public void onMessage(
            Message message,
            byte[] pattern
    ) {

        String payload =
                new String(message.getBody());

        log.info(
                "Redis notification received. channel={}, payload={}",
                notificationChannel,
                payload
        );

        try {

            NotificationEvent event =
                    objectMapper.readValue(
                            payload,
                            NotificationEvent.class
                    );

            notificationDeliveryService.deliver(event);

        } catch (JacksonException exception) {

            log.error(
                    "Failed to deserialize Redis notification event. payload={}",
                    payload,
                    exception
            );
        }
    }
}