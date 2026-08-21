package com.rahul.realtime.notification.service;

public interface NotificationDeliveryDeduplicator {

    boolean shouldDeliver(Long notificationId);
}