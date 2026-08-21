package com.rahul.realtime.notification.service;

import com.rahul.realtime.notification.dto.request.CreateNotificationRequest;
import com.rahul.realtime.notification.dto.response.NotificationPageResponse;
import com.rahul.realtime.notification.dto.response.NotificationResponse;

public interface NotificationService {

    NotificationResponse createNotification(
            CreateNotificationRequest request,
            String userId
    );

    NotificationPageResponse getUserNotifications(
            String userId,
            int page,
            int size
    );

    NotificationResponse markAsRead(Long notificationId, String userId);

    NotificationPageResponse getUnreadNotifications(
            String userId,
            int page,
            int size
    );
}