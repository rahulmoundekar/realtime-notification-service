package com.rahul.realtime.notification.service;

import com.rahul.realtime.notification.dto.event.NotificationEvent;

public interface NotificationPublisher {

    void publish(NotificationEvent event);
    void publishPayload(String payload);
}