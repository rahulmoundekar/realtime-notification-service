package com.rahul.realtime.notification.dto.event;

import com.rahul.realtime.notification.dto.response.NotificationResponse;

import java.time.Instant;

public record NotificationEvent(
        Long notificationId,
        String userId,
        String type,
        String title,
        String message,
        String status,
        Instant createdAt
) {

    public static NotificationEvent from(
            NotificationResponse notification
    ) {
        return new NotificationEvent(
                notification.id(),
                notification.userId(),
                notification.type().name(),
                notification.title(),
                notification.message(),
                notification.status().name(),
                notification.createdAt()
        );
    }
}