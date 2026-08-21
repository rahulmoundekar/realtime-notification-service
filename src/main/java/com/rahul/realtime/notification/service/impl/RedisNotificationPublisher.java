package com.rahul.realtime.notification.service.impl;

import com.rahul.realtime.notification.exception.NotificationPublishException;
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
public class RedisNotificationPublisher implements NotificationPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.redis.notification-channel}")
    private String notificationChannel;

    @Override
    public void publish(NotificationEvent event) {

        try {

            String payload = objectMapper.writeValueAsString(event);

            Long subscriberCount = redisTemplate.convertAndSend(notificationChannel, payload);

            log.info("Redis notification published. channel={}, notificationId={}, subscriberCount={}", notificationChannel, event.notificationId(), subscriberCount);

        } catch (JacksonException exception) {

            log.error("Notification event serialization failed. notificationId={}", event.notificationId(), exception);

            throw new NotificationPublishException("Unable to serialize notification event", exception);

        } catch (Exception exception) {

            log.error("Redis notification publishing failed. notificationId={}, channel={}", event.notificationId(), notificationChannel, exception);

            throw new NotificationPublishException("Unable to publish notification event", exception);
        }
    }

    @Override
    public void publishPayload(String payload) {

        try {

            Long subscriberCount = redisTemplate.convertAndSend(notificationChannel, payload);

            log.info("Redis outbox event published. channel={}, subscriberCount={}", notificationChannel, subscriberCount);

        } catch (Exception exception) {

            throw new NotificationPublishException("Unable to publish outbox event", exception);
        }
    }
}