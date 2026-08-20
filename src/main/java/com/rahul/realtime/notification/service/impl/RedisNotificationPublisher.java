package com.rahul.realtime.notification.service.impl;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.rahul.realtime.notification.dto.event.NotificationEvent;
import com.rahul.realtime.notification.service.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationPublisher
        implements NotificationPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.redis.notification-channel}")
    private String notificationChannel;

    @Override
    public void publish(NotificationEvent event) {

        try {
            String payload =
                    objectMapper.writeValueAsString(event);

            log.info(
                    "Publishing notification. channel={}, notificationId={}, userId={}, payload={}",
                    notificationChannel,
                    event.notificationId(),
                    event.userId(),
                    payload
            );

            Long subscriberCount = redisTemplate.convertAndSend(
                    notificationChannel,
                    payload
            );

            log.info(
                    "Redis publish complete. channel={}, subscriberCount={}",
                    notificationChannel,
                    subscriberCount
            );

        } catch (JacksonException exception) {

            log.error(
                    "Failed to serialize notification event. notificationId={}",
                    event.notificationId(),
                    exception
            );

            throw new IllegalStateException(
                    "Failed to publish notification event",
                    exception
            );
        }
    }
}