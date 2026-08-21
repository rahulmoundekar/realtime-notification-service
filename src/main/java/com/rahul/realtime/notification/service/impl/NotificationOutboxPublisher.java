package com.rahul.realtime.notification.service.impl;

import com.rahul.realtime.notification.entity.NotificationOutboxEvent;
import com.rahul.realtime.notification.repository.NotificationOutboxRepository;
import com.rahul.realtime.notification.service.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationOutboxPublisher {

    private final NotificationOutboxRepository outboxRepository;

    private final NotificationPublisher notificationPublisher;

    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {

        List<NotificationOutboxEvent> events = outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();

        for (NotificationOutboxEvent event : events) {

            try {

                notificationPublisher.publishPayload(event.getPayload());

                event.setPublished(true);

                event.setPublishedAt(Instant.now());

                outboxRepository.save(event);

            } catch (Exception exception) {

                log.error("Outbox publish failed. outboxId={}, notificationId={}", event.getId(), event.getNotificationId(), exception);
            }
        }
    }
}