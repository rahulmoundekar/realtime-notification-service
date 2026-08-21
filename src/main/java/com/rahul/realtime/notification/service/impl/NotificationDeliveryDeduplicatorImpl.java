package com.rahul.realtime.notification.service.impl;

import com.rahul.realtime.notification.service.NotificationDeliveryDeduplicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationDeliveryDeduplicatorImpl implements NotificationDeliveryDeduplicator {

    private final Set<Long> deliveredNotificationIds = ConcurrentHashMap.newKeySet();

    private final int maxEntries;

    public NotificationDeliveryDeduplicatorImpl(@Value("${app.delivery.dedup-max-entries:10000}") int maxEntries) {
        this.maxEntries = maxEntries;
    }

    @Override
    public boolean shouldDeliver(Long notificationId) {

        if (notificationId == null) {
            return true;
        }

        boolean firstDelivery = deliveredNotificationIds.add(notificationId);

        if (deliveredNotificationIds.size() > maxEntries) {

            deliveredNotificationIds.clear();
        }

        return firstDelivery;
    }
}