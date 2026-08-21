package com.rahul.realtime.notification.integration;

import com.rahul.realtime.notification.service.impl.NotificationDeliveryDeduplicatorImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDeliveryDeduplicatorTest {

    @Test
    void shouldAllowFirstDelivery() {

        NotificationDeliveryDeduplicatorImpl deduplicator =
                new NotificationDeliveryDeduplicatorImpl(100);

        assertThat(
                deduplicator.shouldDeliver(100L)
        ).isTrue();
    }

    @Test
    void shouldRejectDuplicateDelivery() {

        NotificationDeliveryDeduplicatorImpl deduplicator =
                new NotificationDeliveryDeduplicatorImpl(100);

        assertThat(
                deduplicator.shouldDeliver(100L)
        ).isTrue();

        assertThat(
                deduplicator.shouldDeliver(100L)
        ).isFalse();
    }

    @Test
    void shouldAllowDifferentNotifications() {

        NotificationDeliveryDeduplicatorImpl deduplicator =
                new NotificationDeliveryDeduplicatorImpl(100);

        assertThat(
                deduplicator.shouldDeliver(100L)
        ).isTrue();

        assertThat(
                deduplicator.shouldDeliver(101L)
        ).isTrue();
    }
}