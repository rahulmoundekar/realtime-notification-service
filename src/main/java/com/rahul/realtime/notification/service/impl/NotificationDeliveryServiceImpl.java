package com.rahul.realtime.notification.service.impl;

import com.rahul.realtime.notification.dto.event.NotificationEvent;
import com.rahul.realtime.notification.service.NotificationDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationDeliveryServiceImpl
        implements NotificationDeliveryService {

    @Override
    public void deliver(NotificationEvent event) {

        log.info(
                "Notification received for delivery. notificationId={}, userId={}, type={}",
                event.notificationId(),
                event.userId(),
                event.type()
        );

        // WebSocket / SSE delivery will be implemented
        // in the next steps.
    }
}