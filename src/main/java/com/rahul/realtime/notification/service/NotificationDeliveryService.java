package com.rahul.realtime.notification.service;

import com.rahul.realtime.notification.dto.event.NotificationEvent;

public interface NotificationDeliveryService {

    void deliver(NotificationEvent event);
}