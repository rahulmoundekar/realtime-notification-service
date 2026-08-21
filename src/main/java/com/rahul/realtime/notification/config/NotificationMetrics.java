package com.rahul.realtime.notification.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetrics {

    private final Counter notificationsCreated;

    private final Counter notificationsDelivered;

    private final Counter deliveryFailures;

    public NotificationMetrics(MeterRegistry meterRegistry) {

        notificationsCreated = Counter.builder("notification.created").description("Notifications created").register(meterRegistry);

        notificationsDelivered = Counter.builder("notification.delivered").description("Notifications delivered").register(meterRegistry);

        deliveryFailures = Counter.builder("notification.delivery.failed").description("Notification delivery failures").register(meterRegistry);
    }

    public void notificationCreated() {
        notificationsCreated.increment();
    }

    public void notificationDelivered() {
        notificationsDelivered.increment();
    }

    public void deliveryFailed() {
        deliveryFailures.increment();
    }
}