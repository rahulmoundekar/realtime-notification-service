package com.rahul.realtime.notification.dto.response;

import com.rahul.realtime.notification.entity.Notification;
import com.rahul.realtime.notification.enums.NotificationStatus;
import com.rahul.realtime.notification.enums.NotificationType;

import java.time.Instant;

public record NotificationResponse(

        Long id,

        String userId,

        NotificationType type,

        String title,

        String message,

        NotificationStatus status,

        Instant createdAt,

        Instant readAt
) {

    public static NotificationResponse from(Notification notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}